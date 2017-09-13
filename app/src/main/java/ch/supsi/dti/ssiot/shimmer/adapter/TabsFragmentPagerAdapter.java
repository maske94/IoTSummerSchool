package ch.supsi.dti.ssiot.shimmer.adapter;


import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ch.supsi.dti.ssiot.shimmer.R;
import ch.supsi.dti.ssiot.shimmer.fragment.BaseTabFragment;

public class TabsFragmentPagerAdapter extends FragmentPagerAdapter {

    /**
     * Tag used for logging purposes
     */
    private static final String TAG = FragmentPagerAdapter.class.getSimpleName();

    /**
     * String representing the tabs fragment's package
     */
    private final String mTabsPackage;

    /**
     * Array of strings containing tabs classes
     */
    private final String[] mTabsClasses;

    /**
     * Array of strings containing tabs titles
     */
    private final String[] mTabsTitles;

    /**
     * Array of icons containing tabs icons
     */
    private final TypedArray mTabsIcons;

    /**
     * Android Context
     */
    private Context mContext;

    /**
     * List of fragments
     */
    private List<BaseTabFragment> mFragments;

    /**
     * Default constructor
     * @param fm the main fragment manager
     * @param context context
     */
    public TabsFragmentPagerAdapter(FragmentManager fm, Context context) {

        super(fm);
        this.mContext   = context;
        this.mFragments = new ArrayList<>();

        // tabs
        this.mTabsPackage = context.getResources().getString(R.string.tabs_package);
        this.mTabsClasses = context.getResources().getStringArray(R.array.tabs_classes);
        this.mTabsTitles  = context.getResources().getStringArray(R.array.tabs_titles);
        this.mTabsIcons   = context.getResources().obtainTypedArray(R.array.tabs_icons);
    }

    @Override
    public int getCount() {
        return mTabsClasses.length;
    }

    @Override
    public Fragment getItem(int position) {

        BaseTabFragment fragment;

        try {

            Method method = Class.forName(String.format("%s.%s", mTabsPackage, mTabsClasses[position])).getMethod("newInstance", Integer.TYPE);
            fragment = (BaseTabFragment) method.invoke(null, position);

        }
        catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {

            fragment = BaseTabFragment.newInstance(position);
            Log.e(TAG, "----> unable to load class: " + mTabsClasses[position]);
        }

        mFragments.add(fragment);

        return fragment;
    }

    /**
     * Gets the view of a tab given its position
     * @param position the position
     * @return a View object
     */
    private View getTabView(int position) {

        View v = LayoutInflater.from(mContext).inflate(R.layout.custom_tab, null);

        // setting the icon of the tab
        ImageView img = (ImageView) v.findViewById(R.id.tabIcon);
        img.setImageDrawable(ContextCompat.getDrawable(mContext, mTabsIcons.getResourceId(position, - 1)));

        // setting the title of the tab
        TextView txt = (TextView) v.findViewById(R.id.tabTitle);
        txt.setText(mTabsTitles[position]);

        return v;
    }

    /**
     * Binds tab layout and view pager
     * @param viewPager the ViewPager
     * @param tabLayout the TabLayout
     */
    public void setup(final ViewPager viewPager, TabLayout tabLayout){

        tabLayout.setupWithViewPager(viewPager);

        // Applying the custom layout for each tab
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setCustomView(this.getTabView(i));
        }
    }

    /**
     * Notify a fragment
     * @param position
     * @param what
     * @param data
     */
    public void notifyFragment(int position, int what, Object data){
        mFragments.get(position).notification(what, data);
    }

    /**
     *
     * @param position
     * @param what
     */
    public void notifyFragment(int position, int what){
        notifyFragment(position, what, null);
    }

    /**
     *
     * @param what
     * @param data
     */
    public void notifyAllFragments(int what, Object data){

        for (BaseTabFragment fragment : mFragments){
            fragment.notification(what, data);
        }
    }

    /**
     *
     * @param what
     */
    public void notifyAllFragments(int what){
        notifyAllFragments(what, null);
    }
}