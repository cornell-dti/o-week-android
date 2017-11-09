package com.cornellsatech.o_week.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Ethan on 10/29/17.
 */

public class InitialSettingsPagerAdapter extends FragmentPagerAdapter {

    public InitialSettingsPagerAdapter(FragmentManager fm){
        super(fm);
    }

    @Override
    public int getCount(){
        return  3;
    }


    public Fragment getItem(int position){
        switch (position) {
            case 0: return new InitialSettingsPage1Fragment();
            case 1: return new InitialSettingsPage2Fragment();
            case 2: return new InitialSettingsPage3Fragment();
            default: return null;
        }
    }


}
