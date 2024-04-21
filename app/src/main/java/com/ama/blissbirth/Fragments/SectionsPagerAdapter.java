package com.ama.blissbirth.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ama.blissbirth.Main;
import com.ama.blissbirth.R;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(Main mainPage, @NonNull FragmentManager fm) {
        super(fm);
    }
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Home_Fragment();
            case 1:
                return new Calendar_Fragment();
            case 2:
                return new Map_Fragment();
            case 3:
                return new Profile_Fragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}
