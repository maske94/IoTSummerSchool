package ch.supsi.dti.ssiot.shimmer;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.tools.Logging;

import ch.supsi.dti.ssiot.shimmer.adapter.TabsFragmentPagerAdapter;

public class MainActivity extends AppCompatActivity {

    /**
     * Tag used for logging purposes
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Int code used as discriminant
     */
    private static final int REQUEST_ENABLE_BT = 0x100;

    /**
     * Service used to connect and grab data from a Shimmer sensor
     */
    private MyService mShimmerService;

    /**
     * Flag that indicates if the service is correctly binded
     */
    private boolean mShimmerServiceBind;

    /**
     * PagerAdapter, it's a fragments holder
     */
    private TabsFragmentPagerAdapter mPagerAdapter;

    /**
     * Request code for external storage writing
     */
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    /**
     * Utils object to log on file
     */
    private Logging mLogger = new Logging("shimmerData");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**
         * Get the ViewPager and sets the PagerAdapter
         */
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        mPagerAdapter = new TabsFragmentPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(mPagerAdapter);
        mPagerAdapter.setup(viewPager, (TabLayout) findViewById(R.id.tablayout));

        /**
         * Bluetooth
         */
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /**
         * Checks if bluetooth is supported
         */
        if (bluetoothAdapter == null) {

            Log.i(TAG, "Bluetooth adapter not supported...");

            // show a dialog...
            new AlertDialog.Builder(this)
                    .setTitle(BluetoothAdapter.class.getSimpleName())
                    .setMessage(getString(R.string.bluetooth_no_adapter))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        /**
         * Checks if bluetooth is enabled, if not, asks for permissions to enable it
         */
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        /**
         * Starts the shimmer service
         */
        Intent startIntent = new Intent(this, MyService.class);
        startIntent.setAction(MyService.ACTION_START);
        startService(startIntent);

        askForWriteExtStoragePermission(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        /**
         * If is the request we are looking for, i.e, the one who asks to enable bluetooth
         */
        if (requestCode == REQUEST_ENABLE_BT) {

            if (resultCode != RESULT_OK) { // something went wrong, i.e, the user pressed "No"

                // show a dialog...
                new AlertDialog.Builder(this)
                        .setTitle(BluetoothAdapter.class.getSimpleName())
                        .setMessage(getString(R.string.bluetooth_not_granted))
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
        }
    }

    @Override
    protected void onStart() {

        super.onStart();
        Log.d(TAG, "onStart()");

        Intent intent = new Intent(this, MyService.class);

        getApplicationContext().bindService(intent, mShimmerServiceConnection, Context.BIND_AUTO_CREATE);

        if (isShimmerServiceRunning()) {
            Log.d(TAG, "Shimmer service started!");
        } else {
            Log.d(TAG, "Shimmer service not started!");
        }
    }

    @Override
    protected void onStop() {

        super.onStop();

        if (mShimmerServiceBind) {
            getApplicationContext().unbindService(mShimmerServiceConnection);
        }
    }

    /**
     * Gets the shimmer service
     *
     * @return a MyService object
     */
    public MyService getShimmerService() {
        return mShimmerService;
    }

    /**
     * Checks if the service is currently running or not
     *
     * @return <code>true</code> if it is running, otherwise <code>false</code>
     */
    protected boolean isShimmerServiceRunning() {

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            if (MyService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    protected ServiceConnection mShimmerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            Log.d(TAG, "---> ServiceConnection#onServiceConnected()");

            MyService.LocalBinder binder = (MyService.LocalBinder) iBinder;
            mShimmerService = binder.getService();
            mShimmerServiceBind = true;
            mPagerAdapter.notifyAllFragments(MyService.NOTIFY_SERVICE_STARTED);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "---> ServiceConnection#onServiceDisconnected()");
            mShimmerServiceBind = false;
        }
    };

    /**
     * If the target sdk version is >= api 23 ( Android 6 (M) )
     * Asks to the user for permission to access the coarse location.
     * This is needed from Android 6 to read ble scanning results.
     *
     * @param activity The activity from where the request is launched
     * @return True if the permission was already granted, false otherwise
     */
    public boolean askForWriteExtStoragePermission(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }

    /**
     *
     */
    public void closeFile(){
        mLogger.closeFile();
        Log.d(TAG, "closeFile() ---> "+mLogger.getAbsoluteName());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "onRequestPermissionsResult() ---> PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE granted");
                } else {
                    Log.i(TAG, "onRequestPermissionsResult() ---> PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE denied");
                }
            }
        }
    }

    public void notifyNewData(Object data) {
        Log.d(TAG, "notifyNewData: ");
        
        /**
         * Log data on file
         */
        if (data instanceof ObjectCluster) {
            ObjectCluster c = (ObjectCluster) data;
            mLogger.logData(c);
        }

        mPagerAdapter.notifyFragment(1, MyService.MESSAGE_NEW_DATA, data);
    }
}
