package ch.supsi.dti.ssiot.shimmer.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.supsi.dti.ssiot.shimmer.MainActivity;

public abstract class BaseTabFragment extends Fragment {

    /**
     * Extras
     */
    public static final String EXTRA_RESOURCE_ID = "BaseTabFragment.ResourceId";
    public static final String EXTRA_FRAGMENT_ID = "BaseTabFragment.FragmentId";

    /**
     * The identification number of the fragment
     */
    protected int mFragmentId;

    /**
     * The resource id of the layout
     */
    private int mResourceId;

    /**
     * The main activity
     */
    protected MainActivity mActivity;

    /**
     * New instance
     * @param fragmentId the id number of the fragment
     * @param resourceId the resource id for fragment's layout
     * @param fragment fragment where to set bundle args
     * @return a new BaseFragment instance
     */
    public static BaseTabFragment newInstance(int fragmentId, int resourceId, BaseTabFragment fragment) {

        if(fragment == null || resourceId == 0){

            fragment   = new EmptyTabFragment();
            resourceId = EmptyTabFragment.LAYOUT_RESOURCE_ID;
        }

        Bundle args = new Bundle();
        args.putInt(EXTRA_FRAGMENT_ID, fragmentId);
        args.putInt(EXTRA_RESOURCE_ID, resourceId);

        fragment.setArguments(args);

        return fragment;
    }

    /**
     * New instance
     * @param fragmentId the id number of the fragment
     * @return a new BaseFragment instance
     */
    public static BaseTabFragment newInstance(int fragmentId) {
        return newInstance(fragmentId, 0, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mFragmentId = getArguments().getInt(EXTRA_FRAGMENT_ID);
        mResourceId = getArguments().getInt(EXTRA_RESOURCE_ID);
        mActivity   = (MainActivity) getActivity();

        View view = inflater.inflate(mResourceId, container, false);
        initMembers();
        initViews(view);

        return view;
    }

    /**
     * Method used to initialize member fields
     */
    public void initMembers() {
        Log.d(getClass().getSimpleName(), "initMembers()");
    }

    /**
     * Method used to initialize views
     * @param rootView the main view
     */
    public abstract void initViews(View rootView);

    /**
     * Method used send notification to fragments
     * @param what which type of notification
     * @param data related data
     */
    public void notification(int what, Object data){
        Log.d(getClass().getSimpleName(), "notification: what = " + what + " data = " + data);
    }
}