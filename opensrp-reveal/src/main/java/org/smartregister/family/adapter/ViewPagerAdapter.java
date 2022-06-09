package org.smartregister.family.adapter;

import android.util.Pair;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private Pair<Integer, Integer> mPositionCountPair;

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = this.mFragmentTitleList.get(position);
        if (mPositionCountPair != null && mPositionCountPair.first == position) {
            title = title + " (" + mPositionCountPair.second + ")";
        }
        return title;
    }

    public void updateCount(Pair<Integer, Integer> positionCountPair) {
        if (positionCountPair.first != null && positionCountPair.second != null) {
            this.mPositionCountPair = positionCountPair;
            notifyDataSetChanged();
        }
    }
}
