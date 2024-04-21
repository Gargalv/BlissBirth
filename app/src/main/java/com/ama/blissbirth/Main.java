package com.ama.blissbirth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.MenuItem;

import com.ama.blissbirth.Fragments.SectionsPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Main extends AppCompatActivity {
    private SectionsPagerAdapter sectionsPagerAdapter;
    private MenuItem prevMenuItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView mybottomNavView = findViewById(R.id.bottom_navigation);
        //page adapter
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        mybottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    item.setChecked(true);

                    viewPager.setCurrentItem(0);
                } else if (item.getItemId() == R.id.calendar) {
                    item.setChecked(true);

                    viewPager.setCurrentItem(1);
                } else if (item.getItemId() == R.id.map) {
                    item.setChecked(true);

                    viewPager.setCurrentItem(2);
                } else if (item.getItemId() == R.id.profile) {
                    item.setChecked(true);

                    viewPager.setCurrentItem(3);
                }
                return false;
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    mybottomNavView.getMenu().getItem(0).setChecked(false);
                    mybottomNavView.getMenu().getItem(position).setChecked(true);

                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}