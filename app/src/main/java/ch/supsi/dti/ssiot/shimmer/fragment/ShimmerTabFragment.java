package ch.supsi.dti.ssiot.shimmer.fragment;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shimmerresearch.android.Shimmer;

import java.util.HashMap;
import java.util.Map;

import ch.supsi.dti.ssiot.shimmer.MainActivity;
import ch.supsi.dti.ssiot.shimmer.MyService;
import ch.supsi.dti.ssiot.shimmer.R;
import ch.supsi.dti.ssiot.shimmer.util.AttemptsData;

public class ShimmerTabFragment extends BaseTabFragment implements DevicesListFragment.OnDeviceSelectedListener{

    /**
     * String used for logging purposes
     */
    private static final String TAG = ShimmerTabFragment.class.getSimpleName();

    /**
     * Support class for this fragment's subviews
     */
    private ViewHolder mViewHolder;

    /**
     * Current selected device
     */
    private BluetoothDevice mSelectedDevice;

    /**
     * Current attempts to connect to the service
     */
    private AttemptsData mCurrentAttemptsData;

    /**
     * Represents the current device state (e.g, none, connected, etc...)
     */
    private int mCurrentDeviceState;

    /**
     * Support fields
     */
    private Map<Integer, Integer> mStateTexts;
    private Map<Integer, Integer> mStateColors;

    /**
     * New instance
     * @param fragmentId the id number of the fragment
     * @return a new BaseFragment instance
     */
    public static BaseTabFragment newInstance(int fragmentId){
        return  BaseTabFragment.newInstance(fragmentId, R.layout.fragment_shimmer, new ShimmerTabFragment());
    }

    @Override
    public void initMembers() {

        mCurrentDeviceState = Shimmer.STATE_NONE;

        // colors
        mStateColors = new HashMap<>();
        mStateColors.put(Shimmer.STATE_NONE, R.drawable.circle_red);
        mStateColors.put(Shimmer.STATE_CONNECTING, R.drawable.circle_yellow);
        mStateColors.put(Shimmer.STATE_CONNECTED, R.drawable.circle_green);
        mStateColors.put(Shimmer.MSG_STATE_FULLY_INITIALIZED, R.drawable.circle_green);
        mStateColors.put(Shimmer.MSG_STATE_STREAMING, R.drawable.circle_blue);
        mStateColors.put(Shimmer.MSG_STATE_STOP_STREAMING, R.drawable.circle_green);

        // texts
        mStateTexts = new HashMap<>();
        mStateTexts.put(Shimmer.STATE_NONE, R.string.state_none);
        mStateTexts.put(Shimmer.STATE_CONNECTING, R.string.state_connecting);
        mStateTexts.put(Shimmer.STATE_CONNECTED, R.string.state_connected);
        mStateTexts.put(Shimmer.MSG_STATE_FULLY_INITIALIZED, R.string.state_initialized);
        mStateTexts.put(Shimmer.MSG_STATE_STREAMING, R.string.state_streaming);
        mStateTexts.put(Shimmer.MSG_STATE_STOP_STREAMING, R.string.state_stop_streaming);
    }

    /**
     * Method used to initialize views
     *
     * @param rootView the main view
     */
    @Override
    public void initViews(View rootView) {

        /**
         * Initializes view holder
         */
        mViewHolder = new ViewHolder(rootView);

        /**
         * Sets up texts and buttons
         */
        mViewHolder.tvDeviceName.setText(R.string.pair_no_sensor_selected);
        mViewHolder.tvDeviceAddress.setText("");
        mViewHolder.btnConnectDisconnect.setEnabled(false);
        mViewHolder.ivSensorState.setImageResource(mStateColors.get(mCurrentDeviceState));
        mViewHolder.tvSensorState.setText(mStateTexts.get(mCurrentDeviceState));

        /**
         * Sets up the click listeners
         */
        mViewHolder.btnSelectSensor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                DialogFragment devicesListDialog = DevicesListFragment.newInstance(ShimmerTabFragment.this);
                devicesListDialog.show(getChildFragmentManager(), DevicesListFragment.class.getSimpleName());
            }
        });
        mViewHolder.btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mCurrentDeviceState == Shimmer.STATE_NONE) {

                    mActivity.getShimmerService().connectAndStream(mSelectedDevice, MyService.CONNECTION_ATTEMPTS);
                    mViewHolder.btnConnectDisconnect.setEnabled(false);
                    mViewHolder.btnSelectSensor.setEnabled(false);
                    mViewHolder.llStateContainer.setVisibility(View.VISIBLE);
                }
                else {

                    mActivity.getShimmerService().disconnect();
                    ((MainActivity)getActivity()).closeFile();
                    mViewHolder.btnConnectDisconnect.setEnabled(true);
                    mViewHolder.btnSelectSensor.setEnabled(true);
                    mViewHolder.llStateContainer.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    /**
     * Called when a bluetooth device is selected from the list of paired devices
     * @param device
     */
    @Override
    public void onDeviceSelected(BluetoothDevice device) {

        Log.d(TAG, "onDeviceSelected(): " + "device = [" + device + "]");
        setupDeviceView(device);
    }

    /**
     * Sets up the device view
     */
    private void setupDeviceView(BluetoothDevice device){

        /**
         * Updates the selected device
         */
        mSelectedDevice = device;

        // TODO: 05/09/16 -> set device to service

        /**
         * Adjusts views
         */
        mViewHolder.tvDeviceName.setText(device.getName().trim());
        mViewHolder.tvDeviceAddress.setText(device.getAddress().trim());
        mViewHolder.btnConnectDisconnect.setEnabled(true);
    }

    @Override
    public void notification(int what, Object data) {

        switch (what){

            case MyService.NOTIFY_SERVICE_STARTED:

                Log.i(TAG, "----> setup service handler");
                mActivity.getShimmerService().setServiceHandler(mServiceHandler);

                break;
        }
    }


    @SuppressLint("HandlerLeak")
    private Handler mServiceHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            // Log.d(TAG, "mServiceHandler#handleMessage() called with: " + "msg = [" + msg + "]");

            switch (msg.what){

                case MyService.MESSAGE_CONNECTION_ATTEMPT:

                    Log.i(TAG, String.format("[%s]: attempt %d of %d", msg.obj.toString(), msg.arg1, msg.arg2));
                    mCurrentAttemptsData =  new AttemptsData(msg.arg2, msg.arg1);
                    // TODO: 05/09/16 ->
                    // mAdapter.setAttemptsData(msg.obj.toString(), new AttemptsData(msg.arg2, msg.arg1));

                    break;

                case Shimmer.MESSAGE_STATE_CHANGE:

                    Log.i(TAG, String.format("[%s] State change of %d", msg.obj.toString(), msg.arg1));

                    mCurrentDeviceState = msg.arg1;
                    mViewHolder.ivSensorState.setImageResource(mStateColors.get(mCurrentDeviceState));

                    if (mCurrentAttemptsData != null && mCurrentDeviceState == Shimmer.STATE_CONNECTING){

                        String format = getActivity().getString(R.string.state_connecting_attempts);
                        mViewHolder.tvSensorState.setText(String.format(format, mCurrentAttemptsData.getAttempts(), mCurrentAttemptsData.getTotalAttempts()));
                    }
                    else{

                        mViewHolder.tvSensorState.setText(mStateTexts.get(mCurrentDeviceState));
                    }

//                    mViewHolder.ivSensorState.setImageResource(mStateColors.get(mCurrentDeviceState));
//                    mViewHolder.tvSensorState.setText(mStateTexts.get(mCurrentDeviceState));

                    switch (mCurrentDeviceState){

                        case Shimmer.STATE_NONE:

                            mViewHolder.btnConnectDisconnect.setText(R.string.fragment_shimmer_connect);

                            Log.d(TAG, "attempts: " + mCurrentAttemptsData.getAttempts());

                            if (mCurrentAttemptsData.getAttempts() == MyService.CONNECTION_ATTEMPTS){

                                mViewHolder.btnConnectDisconnect.setEnabled(true);
                                mViewHolder.btnSelectSensor.setEnabled(true);
                                mViewHolder.llStateContainer.setVisibility(View.INVISIBLE);
                            }

                            break;

                        case Shimmer.MSG_STATE_FULLY_INITIALIZED:

                            mViewHolder.btnConnectDisconnect.setText(R.string.fragment_shimmer_disconnect);
                            mViewHolder.btnConnectDisconnect.setEnabled(true);
                            mViewHolder.btnSelectSensor.setEnabled(false);

                            break;

                        default:
                            break;
                    }

                    break;

                case MyService.MESSAGE_NEW_DATA:
                    mActivity.notifyNewData(msg.obj);

                    break;

                default:
                    break;
            }
        }
    };

    /**
     * View holder class: utils to hold subviews
     */
    private static class ViewHolder {

        TextView tvDeviceName;
        TextView tvDeviceAddress;
        Button btnSelectSensor;
        Button btnConnectDisconnect;
        LinearLayout llStateContainer;
        ImageView ivSensorState;
        TextView tvSensorState;


        public ViewHolder(View rootView){

            tvDeviceName         = (TextView) rootView.findViewById(R.id.tvDeviceName);
            tvDeviceAddress      = (TextView) rootView.findViewById(R.id.tvDeviceAddress);
            btnSelectSensor      = (Button) rootView.findViewById(R.id.btnSelectSensor);
            btnConnectDisconnect = (Button) rootView.findViewById(R.id.btnConnectDisconnect);
            llStateContainer     = (LinearLayout) rootView.findViewById(R.id.llStateContainer);
            ivSensorState        = (ImageView) rootView.findViewById(R.id.ivSensorState);
            tvSensorState        = (TextView) rootView.findViewById(R.id.tvSensorState);
        }
    }
}
