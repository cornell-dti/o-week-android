package com.cornellsatech.o_week;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cornellsatech.o_week.models.CollegeType;

import java.util.ArrayList;

/**
 *
 * The pager fragment for the college settings page.
 */

public class InitialSettingsPage2Fragment extends Fragment {

    private Button ALS;
    private Button AAP;
    private Button AS;
    private Button ENG;
    private Button ILR;
    private Button HE;
    private Button JOHNSON;
    private CollegeType collegeType = CollegeType.NOTSET;

    /**
     * Sets up the views
     * Add action listener to each button.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.initial_settings_college, container,false);

        ALS = rootView.findViewById(R.id.ALS);
        AAP = rootView.findViewById(R.id.AAP);
        AS = rootView.findViewById(R.id.AS);
        ENG = rootView.findViewById(R.id.ENG);
        ILR = rootView.findViewById(R.id.ILR);
        HE = rootView.findViewById(R.id.HE);
        JOHNSON = rootView.findViewById(R.id.Johnson);

        ALS.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setActiveButton(ALS);
                collegeType = CollegeType.ALS;
                jumpToNextFragment();
            }
        });

        AAP.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setActiveButton(AAP);
                collegeType = CollegeType.AAP;
                jumpToNextFragment();
            }
        });

        AS.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setActiveButton(AS);
                collegeType = CollegeType.AS;
                jumpToNextFragment();
            }
        });

        ENG.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setActiveButton(ENG);
                collegeType = CollegeType.ENG;
                jumpToNextFragment();
            }
        });

        ILR.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setActiveButton(ILR);
                collegeType = CollegeType.ILR;
                jumpToNextFragment();
            }
        });

        HE.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setActiveButton(HE);
                collegeType = CollegeType.HE;
                jumpToNextFragment();
            }
        });

        JOHNSON.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setActiveButton(JOHNSON);
                collegeType = CollegeType.JOHNSON;
                jumpToNextFragment();
            }
        });


        return rootView;
    }

    /**
     * Set the given button as active, other buttons in the class as inactive.
     * @param button
     */
    private void setActiveButton(Button button) {
        ArrayList<Button> buttonGroup = new ArrayList<Button>();
        buttonGroup.add(ALS);
        buttonGroup.add(AAP);
        buttonGroup.add(AS);
        buttonGroup.add(ENG);
        buttonGroup.add(ILR);
        buttonGroup.add(HE);
        buttonGroup.add(JOHNSON);
        setSelected(button);
        for(Button b : buttonGroup) {
            if(b != button) {
                disableSelected(b);
            }
        }

    }

    /**
     * Set a button's style as selected
     * @param button
     */
    private void setSelected(Button button){
        button.setBackgroundResource(R.drawable.bg_button_selected_ripple);
        button.setTextColor(Color.WHITE);
    }

    /**
     * set a button's style as not selected
     * @param button
     */
    private void disableSelected(Button button){
        button.setBackgroundResource(R.drawable.bg_button_ripple);
        button.setTextColor(ContextCompat.getColor(this.getContext(), R.color.colorPrimary));
    }

    /**
     * switch to the next page of initial settings.
     */
    private void jumpToNextFragment(){
        if(collegeType != CollegeType.NOTSET) {
            InitialSettingsActivity parentActivity = (InitialSettingsActivity)getActivity();
            parentActivity.setCollegeType(collegeType);
            parentActivity.switchToNextPage();
        }
    }
}
