package com.cornellsatech.o_week;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cornellsatech.o_week.models.InternationalStudentStatus;
import com.cornellsatech.o_week.models.StudentType;


/**
 *
 * The pager fragment for the student type initial settings.
 */

public class InitialSettingsPage1Fragment extends Fragment{

    private Button freshman;
    private Button transfer;
    private Button yes;
    private Button no;
    private StudentType studentType = StudentType.NOTSET;
    private InternationalStudentStatus internationalStudentStatus = InternationalStudentStatus.NOTSET;
    private InitialSettingsActivity parentActivity;

    /**
     * Sets up the view components of the student type settings.
     * add action listener to each button.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.initial_settings_student_type, container,false);
        freshman = rootView.findViewById(R.id.freshman);
        transfer = rootView.findViewById(R.id.transfer);
        yes = rootView.findViewById(R.id.isInternationalStudent);
        no = rootView.findViewById(R.id.notInternationalStudent);

        if(studentType == StudentType.FRESHMAN) {
            setSelected(freshman);
            disableSelected(transfer);
        }
        if(studentType == StudentType.TRANSFER) {
            setSelected(transfer);
            disableSelected(freshman);
        }
        if(internationalStudentStatus == InternationalStudentStatus.YES) {
            setSelected(yes);
            disableSelected(no);
        }
        if(internationalStudentStatus == InternationalStudentStatus.NO){
            setSelected(no);
            disableSelected(yes);
        }
        //Sometimes view is reloaded after scrolling back from the third page. This ensures that the options the user had chosen remains visible.

        freshman.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setSelected(freshman);
                disableSelected(transfer);
                studentType = StudentType.FRESHMAN;
                jumpToNextFragment();
            }
        });

        transfer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setSelected(transfer);
                disableSelected(freshman);
                studentType = StudentType.TRANSFER;
                jumpToNextFragment();
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setSelected(yes);
                disableSelected(no);
                internationalStudentStatus = InternationalStudentStatus.YES;
                jumpToNextFragment();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setSelected(no);
                disableSelected(yes);
                internationalStudentStatus = InternationalStudentStatus.NO;
                jumpToNextFragment();
            }
        });

        return rootView;
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
        if(studentType != StudentType.NOTSET && internationalStudentStatus != InternationalStudentStatus.NOTSET) {
            InitialSettingsActivity parentActivity = (InitialSettingsActivity)getActivity();
            parentActivity.setStudentType(studentType);
            parentActivity.setInternationalStudentStatus(internationalStudentStatus);
            parentActivity.switchToNextPage();

        }
    }


}
