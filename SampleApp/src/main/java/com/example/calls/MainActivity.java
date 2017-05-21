/**************************************************************************************************
 * Copyright (C) 2016 WickerLabs. inc - All Rights Reserved.                                      *
 *                                                                                                *
 * NOTICE:  All information contained herein is, and remains the property of WickerLabs,          *
 * The intellectual and technical concepts contained herein are proprietary to WickerLabs.        *
 * Dissemination of this information or reproduction of this material                             *
 * is strictly forbidden unless prior permission is obtained from WickerLabs. inc                 *
 *                                                                                                *
 **************************************************************************************************/
package com.example.calls;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.calls.Adapter.FragAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by littl on 2017/5/15.
 */

public class MainActivity extends AppCompatActivity{
    private View contactButton, calllogButton;
    private FragmentTransaction ft;
    private List<Fragment> fragments = new ArrayList<Fragment>();
    private RadioGroup mGroup;
    private ViewPager mPager;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        //construct Adapter

        //fragments.add(new FragmentCalllogs());
        fragments.add(new FragmentCalllogs());
        fragments.add(new FragmentContacts());
        //fragments.add(new FragmentContacts());
        FragAdapter fragAdapter = new FragAdapter(getSupportFragmentManager(), fragments);

        //set Adapter
        mPager = (ViewPager)findViewById(R.id.mViewPager);
        mPager.setOnPageChangeListener(new PageChangeListener());
        mPager.setAdapter(fragAdapter);
        mGroup = (RadioGroup) findViewById(R.id.group);
        mGroup.setOnCheckedChangeListener(new CheckedChangeListener());
        mGroup.check(R.id.button_calllogs);


    }


    private class CheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.button_calllogs:
                    mPager.setCurrentItem(0);
                    break;
                case R.id.button_contacts:
                    mPager.setCurrentItem(1);
                    break;
            }
        }
    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    mGroup.check(R.id.button_calllogs);
                    break;
                case 1:
                    mGroup.check(R.id.button_contacts);
                    break;

            }
        }
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

    }




}
