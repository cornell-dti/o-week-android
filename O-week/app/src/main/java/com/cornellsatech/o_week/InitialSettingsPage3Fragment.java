package com.cornellsatech.o_week;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 *
 * the pager fragment for the welcome page.
 */

public class InitialSettingsPage3Fragment extends Fragment {

    private Button getStarted;


    /**
     * Sets up the view components.
     * Add action listener for button.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.initial_settings_welcome, container,false);

        getStarted = rootView.findViewById(R.id.getStartedButton);

        getStarted.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                InitialSettingsActivity parentActivity = (InitialSettingsActivity)getActivity();
                parentActivity.switchToNextPage();
            }
        });

        return rootView;
    }
}
