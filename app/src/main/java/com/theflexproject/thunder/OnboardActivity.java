package com.theflexproject.thunder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.theflexproject.thunder.fragments.AddNewIndexFragment;
import com.xcode.onboarding.MaterialOnBoarding;
import com.xcode.onboarding.OnBoardingPage;
import com.xcode.onboarding.OnFinishLastPage;

import java.util.ArrayList;
import java.util.List;

public class OnboardActivity extends AppCompatActivity {

    AddNewIndexFragment scanFragment = new AddNewIndexFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main)  ---> Remove this line
        List<OnBoardingPage> pages = new ArrayList<>();
        pages.add(new OnBoardingPage(R.drawable.nfgplus, Color.BLACK,Color.WHITE,
                "Welcome to nfgplus",
                ""));
        pages.add(new OnBoardingPage(R.drawable.ss2, Color.BLACK,Color.WHITE,
                "Change User Picture",
                "You can change your profile picture at settings menu"));
        pages.add(new OnBoardingPage(R.drawable.ss1, Color.BLACK,Color.WHITE,
                "Scan Movie First",
                "After the intro finish, you need to scan movie first to load the movies"));


        MaterialOnBoarding OnBoarder = new MaterialOnBoarding();
        OnBoarder.setupOnBoarding((Activity) this, (ArrayList<OnBoardingPage>) pages, new OnFinishLastPage() {
            @Override
            public void onNext() {
                // this is called when user click finish button on last page.
                startActivity(new Intent(OnboardActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}