package com.parse.starter.varescon.AdminHistoryHolder;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.starter.varescon.History.ReserveHistory;
import com.parse.starter.varescon.History.ReserveHistoryDriver;
import com.parse.starter.varescon.History.TripHistory;
import com.parse.starter.varescon.History.TripHistoryDriver;
import com.parse.starter.varescon.R;
import com.parse.starter.varescon.SectionPageAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class AdminHolder extends Fragment {

    private ViewPager mViewPager;
    private String CustorDriver;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history_holder, container, false);

        mViewPager = v.findViewById(R.id.view_pager);
        TabLayout tabLayout = v.findViewById(R.id.tab_layout);
        mViewPager.setOffscreenPageLimit(2);

        setupViewPager(mViewPager);
        tabLayout.setupWithViewPager(mViewPager);
        return v;
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionPageAdapter adapter = new SectionPageAdapter(getChildFragmentManager());
        adapter.addFragment(new TripHistoryAdmin(), "Trip History");
        adapter.addFragment(new ReserveHistoryAdmin(), "Reserve");
        viewPager.setAdapter(adapter);
    }
}
