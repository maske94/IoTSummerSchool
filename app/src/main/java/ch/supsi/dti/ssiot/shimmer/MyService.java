package ch.supsi.dti.ssiot.shimmer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.tools.Logging;

import ch.supsi.dti.ssiot.shimmer.util.AttemptsData;

public class MyService extends Service {

    /**
     * Tag used for logging purposes
     */
    private static final String TAG = MyService.class.getSimpleName();

    /**
     * Service's constants
     */
    public static final String ACTION_START = "ForegroundService.StartForeground";
    public static final String ACTION_STOP = "ForegroundService.StopForeground";
    public static final String ACTION_MAIN = "ForegroundService.Main";
    public static final int SERVICE_NOTIFICATION = 300;
    public static final int MESSAGE_CONNECTION_ATTEMPT = 600;
    public static final int MESSAGE_NEW_DATA = 700;
    public static final int NOTIFY_SERVICE_STARTED = 200;
    public static final int CONNECTION_ATTEMPTS = 5;

    /**
     * Binds and unbinds the service
     */
    private LocalBinder mBinder;

    /**
     * Shimmer object, used to connect and disconnect
     */
    private Shimmer mShimmer;

    /**
     * Flag that indicates whether the Shimmer is connected or not
     */
    private boolean mIsSensorConnected;

    /**
     * Wrapper that indicates the number of attempts of connection
     */
    private AttemptsData mConnectionAttempts;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null) {
            Log.d(TAG, "onStartCommand() called with: " + "action = " + intent.getAction() + ", flags = " + flags + ", startId = " + startId + "");

            switch (intent.getAction()) {

                case ACTION_START:

                    Log.i(TAG, "START FOREGROUND");

                    Intent notificationIntent = new Intent(this, MainActivity.class);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    notificationIntent.setAction(ACTION_MAIN);

                    PendingIntent pi = PendingIntent.getService(this, 0, notificationIntent, 0);

                    Intent stopIntent = new Intent(this, MyService.class);
                    stopIntent.setAction(ACTION_STOP);

                    PendingIntent psi = PendingIntent.getService(this, 0, stopIntent, 0);

                    startForeground(SERVICE_NOTIFICATION, getNotification(pi, psi));

                    setupMembers();

                    break;

                case ACTION_STOP:

                    Log.i(TAG, "STOP FOREGROUND");

                    stopForeground(true);
                    stopSelf();

                    break;

                default:
                    break;
            }
        }

        return START_STICKY;
    }

    /**
     * Creates the notification view for the service
     *
     * @return a Notification object
     */
    private Notification getNotification(PendingIntent contentIntent, PendingIntent stopIntent) {

        return new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent)
                .addAction(android.R.drawable.ic_delete, "Stop", stopIntent)
                .setOngoing(true)
                .setLocalOnly(true)
                .build();
    }

    /**
     * Sets up class members
     */
    private void setupMembers() {

    }

    /**
     * Connects to the Shimmer device
     *
     * @param device           the bluetooth device that represents the Shimmer
     * @param numberOfAttempts the number of attempts
     */
    public void connectAndStream(BluetoothDevice device, int numberOfAttempts) {

        if (!mIsSensorConnected) {

            mConnectionAttempts = new AttemptsData(numberOfAttempts);

            // mShimmer = new Shimmer(this, mConnectionHandler, device.getName(), 51.2, 0, 0, 128, false);
            mShimmer = new Shimmer(this, mConnectionHandler, device.getName(), false);
            mShimmer.connect(device.getAddress(), "default");
        } else if (!mShimmer.isStreaming()) {
            mShimmer.startStreaming();
        }
    }

    /**
     * Used for calling the n-th attempt of connection for one sensor
     *
     * @param bluetoothAddress
     */
    private void connectAndStream(String bluetoothAddress) {

        if (mShimmer != null && mShimmer.getBluetoothAddress().equals(bluetoothAddress)) {
            mShimmer.connect(bluetoothAddress, "default");
        }
    }

    /**
     * Disconnects the shimmer only if it is connected
     */
    public void disconnect() {

        if (mShimmer != null) {
            mShimmer.stop();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mConnectionHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            // Log.d(TAG, "mHandler#handleMessage(): what = " + msg.what + " arg1 = " + msg.arg1 + " arg2 = " + msg.arg2 + " obj = " + msg.obj);

            switch (msg.what) {

                case Shimmer.MESSAGE_CONNECTION_FAILED:

                    Log.i("Connection", String.format("[%s]: attempt %d of %d failed", msg.obj.toString(), mConnectionAttempts.getAttempts(), mConnectionAttempts.getTotalAttempts()));

                    if (!mConnectionAttempts.addAttempt()) {
                        connectAndStream(msg.obj.toString());
                    }

                    break;

                case Shimmer.MESSAGE_CONNECTION_LOST:

                    Log.i(TAG, "Connection lost: " + msg.obj.toString());

                    break;

                // 1
                case Shimmer.MESSAGE_STATE_CHANGE:

                    final ObjectCluster cluster = (ObjectCluster) msg.obj;

                    switch (msg.arg1) {

                        case Shimmer.STATE_NONE:

                            Log.i(TAG, "Sensor " + cluster.mBluetoothAddress + " not connected!");
                            mIsSensorConnected = false;

                            break;

                        case Shimmer.STATE_CONNECTING:

                            AttemptsData aa = mConnectionAttempts != null ? mConnectionAttempts : new AttemptsData(1);
                            mServiceHandler.obtainMessage(MESSAGE_CONNECTION_ATTEMPT, aa.getAttempts(), aa.getTotalAttempts(), cluster.mBluetoothAddress).sendToTarget();

                            break;

                        case Shimmer.STATE_CONNECTED:

                            mConnectionAttempts = null;
                            mIsSensorConnected = true;

                            break;

                        case Shimmer.MSG_STATE_FULLY_INITIALIZED:

                            Log.i(TAG, "Sensor " + cluster.mBluetoothAddress + " --> startStreaming()");
                            mShimmer.startStreaming();

                            break;

                        default:
                            break;
                    }

                    mServiceHandler.obtainMessage(msg.what, msg.arg1, 0, cluster.mBluetoothAddress).sendToTarget();

                    break;

                // 2
                case Shimmer.MESSAGE_READ:

                    // within each msg an object can be include, object clusters are used to represent the data structure of the shimmer device
                    if ((msg.obj instanceof ObjectCluster)) {


                        //ObjectCluster c = (ObjectCluster) msg.obj;
                        //Log.i(TAG, "New data --> " + Arrays.toString(c.mCalData));
                        mServiceHandler.obtainMessage(MESSAGE_NEW_DATA, 0, 0, msg.obj).sendToTarget();
                    }else{
                        Log.e(TAG, "handleMessage: obg IS NOT a ObjectCluster");
                    }

                    break;

                // 6
                case Shimmer.MESSAGE_TOAST:

                    Toast.makeText(MyService.this, msg.getData().getString(Shimmer.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * @param serviceHandler
     */
    public void setServiceHandler(Handler serviceHandler) {
        mServiceHandler = serviceHandler;
    }

    /**
     * Default service handler
     */
    @SuppressLint("HandlerLeak")
    private Handler mServiceHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Log.d(TAG, "MyService#handleMessage() called with: " + "msg = [" + msg + "]");
            super.handleMessage(msg);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        mBinder = new LocalBinder();
        return mBinder;
    }

    /**
     * Local binder, used to bind and unbind the service
     */
    public class LocalBinder extends Binder {

        public MyService getService() {
            return MyService.this;
        }
    }
}
