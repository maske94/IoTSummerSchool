package ch.supsi.dti.ssiot.shimmer.fragment;

import android.view.View;
import android.widget.TextView;

import com.shimmerresearch.driver.ObjectCluster;

import ch.supsi.dti.ssiot.shimmer.MyService;
import ch.supsi.dti.ssiot.shimmer.R;

/**
 * Created by Gianluca Costante on 05/09/16.
 */
public class DataTabFragment extends  BaseTabFragment{

    /**
     * String used for logging purposes
     */
    private static final String TAG = EmptyTabFragment.class.getSimpleName();

    /**
     * Resource id for the empty layout
     */
    private static final int LAYOUT_RESOURCE_ID = R.layout.fragment_data;

    /**
     * Support class for this fragment's subviews
     */
    private ViewHolder mViewHolder;

    /**
     * New instance
     * @param fragmentId the id number of the fragment
     * @return a new BaseFragment instance
     */
    public static BaseTabFragment newInstance(int fragmentId){
        return BaseTabFragment.newInstance(fragmentId, LAYOUT_RESOURCE_ID, new DataTabFragment());
    }

    @Override
    public void initViews(View rootView) {

        mViewHolder = new ViewHolder(rootView);
    }

    @Override
    public void notification(int what, Object data) {

        if (what == MyService.MESSAGE_NEW_DATA){
            mViewHolder.displayObjectCluster((ObjectCluster) data, false);
        }
    }

    private class ViewHolder {

        TextView tvTimestampValue;
        TextView tvXValue;
        TextView tvYValue;
        TextView tvZValue;

        ViewHolder(View rootView){

            tvTimestampValue = (TextView) rootView.findViewById(R.id.tvTimestampValue);
            tvXValue = (TextView) rootView.findViewById(R.id.tvXValue);
            tvYValue = (TextView) rootView.findViewById(R.id.tvYValue);
            tvZValue = (TextView) rootView.findViewById(R.id.tvZValue);
        }

        void displayObjectCluster(ObjectCluster cluster, boolean calibrated){

            double[] data = calibrated ? cluster.mCalData : cluster.mUncalData;
            String format = calibrated ? "%.3f m/s^2" : "%.3f";

            tvTimestampValue.setText(String.valueOf(data[0]));
            tvXValue.setText(String.format(format, data[1]));
            tvYValue.setText(String.format(format, data[2]));
            tvZValue.setText(String.format(format, data[3]));
        }
    }
}
