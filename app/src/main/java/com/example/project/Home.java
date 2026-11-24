package com.example.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.project.ui.admin.AdminFragment;
import com.example.project.ui.home.HomeFragment;
import com.example.project.ui.reflow.ReflowFragment;
import com.example.project.ui.transform.TransformFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SharedPreferences sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        setupMenu(bottomNavigationView.getMenu(), isAdmin);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                openPage(item.getItemId());
                return true;
            }
        });

        if (isAdmin) {
            openPage(R.id.nav_admin);
            bottomNavigationView.setSelectedItemId(R.id.nav_admin);
        } else {
            openPage(R.id.nav_home);
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private void setupMenu(Menu menu, boolean isAdmin) {
        menu.findItem(R.id.nav_home).setVisible(true);
        menu.findItem(R.id.nav_transform).setVisible(!isAdmin);
        menu.findItem(R.id.nav_reflow).setVisible(!isAdmin);
        menu.findItem(R.id.nav_admin).setVisible(isAdmin);
    }

    public void openPage(int pageId) {
        Fragment fragment;

        if (pageId == R.id.nav_transform) {
            fragment = new TransformFragment();
        } else if (pageId == R.id.nav_reflow) {
            fragment = new ReflowFragment();
        } else if (pageId == R.id.nav_admin) {
            fragment = new AdminFragment();
        } else {
            fragment = new HomeFragment();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.pageContainer, fragment)
                .commit();
    }
}

