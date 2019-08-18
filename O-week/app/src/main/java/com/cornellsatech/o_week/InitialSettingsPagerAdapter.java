package com.cornellsatech.o_week;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import lombok.Getter;

/**
 * Created by Ethan on 10/29/17.
 *
 * The pager adapter for the initial settings, including student type, college, and welcome.
 * {@link #progress} : the progress of the user selection. If user have finished the first page, the progress should increment to one.
 */

public class InitialSettingsPagerAdapter extends FragmentPagerAdapter {
    @Getter
    private int progress = 0;

    public InitialSettingsPagerAdapter(FragmentManager fm){
        super(fm);
    }

    /**
     * advance one progress. This indicates that the user has finished selecting items on one page.
     */
    public void advanceProgress(){
        progress++;
    }

    @Override
    public int getCount(){
        return progress + 1; //the user cannot scroll pass his progress.
    }

    /**
     * Controls which fragment to return at each position. This method is called by the calling PagerAdapter automatically by java.
     * @param position
     * @return
     */
    public Fragment getItem(int position){
        switch (position) {
            case 0: return new InitialSettingsPage1Fragment();
            case 1: return new InitialSettingsPage2Fragment();
            case 2: return new InitialSettingsPage3Fragment();
            default: return null;
        }
    }
}
