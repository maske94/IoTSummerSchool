package ch.supsi.dti.ssiot.shimmer.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ch.supsi.dti.ssiot.shimmer.R;
import ch.supsi.dti.ssiot.shimmer.adapter.PairedDevicesArrayAdapter;

/**
 * Class used to display bluetooth paired devices
 */
public class DevicesListFragment extends DialogFragment {

    /**
     * String used for logging purposes
     */
    private static final String TAG = DevicesListFragment.class.getSimpleName();

    /**
     * Bluetooth adapter, used to grab paired devices
     */
    private BluetoothAdapter mBluetoothAdapter;

    /**
     * Device selected callback
     */
    private OnDeviceSelectedListener mSelectionListener;

    /**
     * New instance
     * @return a DeviceListFragment instance
     */
    public static DevicesListFragment newInstance(OnDeviceSelectedListener selectionListener) {

        DevicesListFragment dialogFragment = new DevicesListFragment();
        dialogFragment.setSelectionListener(selectionListener);

        dialogFragment.setShowsDialog(false);
        dialogFragment.setCancelable(false);

        return dialogFragment;
    }

    /**
     * Sets the selection listener
     * @param selectionListener the listener to set
     */
    public void setSelectionListener(OnDeviceSelectedListener selectionListener) {
        mSelectionListener = selectionListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.fragment_devices_list, container, false);

        /**
         * Bluetooth
         */
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final List<BluetoothDevice> devices = new ArrayList<>(mBluetoothAdapter.getBondedDevices());

        /**
         * Sets up list view
         */
        ListView lvPairedDevices = (ListView) rootView.findViewById(R.id.lvPairedDevices);
        lvPairedDevices.setAdapter(new PairedDevicesArrayAdapter(getContext(), devices));
        lvPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            /**
             * Callback method to be invoked when an item in this AdapterView has
             * been clicked.
             * <p/>
             * Implementers can call getItemAtPosition(position) if they need
             * to access the data associated with the selected item.
             *
             * @param parent   The AdapterView where the click happened.
             * @param view     The view within the AdapterView that was clicked (this
             *                 will be a view provided by the adapter)
             * @param position The position of the view in the adapter.
             * @param id       The row id of the item that was clicked.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                /**
                 * If we have a listener in place, call it
                 */
                if (mSelectionListener != null){
                    mSelectionListener.onDeviceSelected(devices.get(position));
                }

                DevicesListFragment.this.dismiss();
            }
        });

        return rootView;
    }

    /**
     * Callback class
     */
    public interface OnDeviceSelectedListener {

        /**
         * Called when a bluetooth device is selected from the list of paired devices
         * @param device
         */
        void onDeviceSelected(BluetoothDevice device);
    }
}