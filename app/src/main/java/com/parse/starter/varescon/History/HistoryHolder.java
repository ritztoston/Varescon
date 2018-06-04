package com.parse.starter.varescon.History;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.parse.starter.varescon.History.HistoryRecycler.HistorySinglePage;
import com.parse.starter.varescon.R;
import com.parse.starter.varescon.SectionPageAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryHolder extends Fragment {

    private ViewPager mViewPager;
    private String CustorDriver;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history_holder, container, false);

        mViewPager = v.findViewById(R.id.view_pager);
        TabLayout tabLayout = v.findViewById(R.id.tab_layout);
        mViewPager.setOffscreenPageLimit(2);

        if (getArguments().getString("CustorDriver") != null)
            CustorDriver = getArguments().getString("CustorDriver");

        setupViewPager(mViewPager);





        tabLayout.setupWithViewPager(mViewPager);
        return v;
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionPageAdapter adapter = new SectionPageAdapter(getChildFragmentManager());
        if(CustorDriver.equals("Customer")) {
            adapter.addFragment(new TripHistory(), "Trip History");
            adapter.addFragment(new ReserveHistory(), "Reserve");
            viewPager.setAdapter(adapter);
        }
        else if(CustorDriver.equals("Driver")){
            adapter.addFragment(new TripHistoryDriver(), "Trip History");
            adapter.addFragment(new ReserveHistoryDriver(), "Reserve");
            viewPager.setAdapter(adapter);
        }
    }
}
