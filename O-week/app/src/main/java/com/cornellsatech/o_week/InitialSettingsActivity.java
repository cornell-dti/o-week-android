package com.cornellsatech.o_week;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.cornellsatech.o_week.models.CollegeType;
import com.cornellsatech.o_week.models.InternationalStudentStatus;
import com.cornellsatech.o_week.models.StudentType;
import com.cornellsatech.o_week.util.NotificationCenter;
import com.cornellsatech.o_week.util.Settings;

/**
 * Controls interactions of the user initial settings. (student type, whether is international, which college)
 *
 * {@Link #INITIAL_SETTINGS_PAGENUM} : The number of pages that the initial settings contains
 */

public class InitialSettingsActivity extends AppCompatActivity {

    private final int INITIAL_SETTINGS_PAGENUM = 3;
    private ViewPager pager;
    private InitialSettingsPagerAdapter adapter;
    private StudentType studentType = StudentType.NOTSET;
    private InternationalStudentStatus internationalStudentStatus = InternationalStudentStatus.NOTSET;
    private CollegeType collegeType = CollegeType.NOTSET;


    /**
     * Initialize the view pager for initial settings. the view pager is locked so that user cannot scroll to bypass the selection.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.initial_settings_pager);
        pager = findViewById(R.id.initialSettingsPager);
        adapter = new InitialSettingsPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
    }

    /**
     * Switch to the next page of the initial settings. If all settings are complete, this activity is finished.
     */
    public void switchToNextPage(){
        if(pager.getCurrentItem() < INITIAL_SETTINGS_PAGENUM - 1) {
            if(pager.getCurrentItem() == adapter.getProgress()) {
                adapter.advanceProgress();
                adapter.notifyDataSetChanged();
            }
            pager.setCurrentItem(pager.getCurrentItem() + 1, true);
        }
        else {
            if(studentType != StudentType.NOTSET && internationalStudentStatus != InternationalStudentStatus.NOTSET && collegeType != CollegeType.NOTSET) {
                Settings.setStudentInfo(this, studentType, internationalStudentStatus, collegeType);
                NotificationCenter.DEFAULT.post(new NotificationCenter.EventReload());
                finish();
            }
        }
    }

    /**
     * Make sure when back is pressed, the app goes home instead of the main activity so that user cannot bypass the selection.
     */
    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);

    }

    /**
     * Sets {@Link #studentType}
     * @param s
     */
    public void setStudentType(StudentType s){
        studentType = s;
    }

    /**
     * Sets {@Link #internationalStudentStatus}
     * @param i
     */
    public void setInternationalStudentStatus(InternationalStudentStatus i) {
        internationalStudentStatus = i;
    }

    /**
     * Sets {@Link #collegeType}
     * @param c
     */
    public void setCollegeType(CollegeType c) {
        collegeType = c;
    }
}
