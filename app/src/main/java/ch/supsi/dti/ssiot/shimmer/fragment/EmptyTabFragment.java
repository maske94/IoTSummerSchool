package ch.supsi.dti.ssiot.shimmer.fragment;

import android.util.Log;
import android.view.View;

import ch.supsi.dti.ssiot.shimmer.R;

public class EmptyTabFragment extends BaseTabFragment {

    /**
     * String used for logging purposes
     */
    private static final String TAG = EmptyTabFragment.class.getSimpleName();

    /**
     * Resource id for the empty layout
     */
    public static final int LAYOUT_RESOURCE_ID = R.layout.fragment_empty;

    /**
     * New instance
     * @param fragmentId the id number of the fragment
     * @return a new BaseFragment instance
     */
    public static BaseTabFragment newInstance(int fragmentId){
        return BaseTabFragment.newInstance(fragmentId, 0, null);
    }

    @Override
    public void initViews(View rootView) {

    }

    @Override
    public void notification(int what, Object data) {

        Log.d(TAG, "notification(): " + "what = [" + what + "], data = [" + data + "]");
    }
}