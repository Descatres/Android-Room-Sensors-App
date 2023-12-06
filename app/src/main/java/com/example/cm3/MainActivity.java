package com.example.cm3;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private HomeFragment homeFragment;
    private GraphFragment graphFragment;
    private AlertsFragment alertsFragment;

    private final Map<Integer, Fragment> fragmentMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideSystemUI();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        homeFragment = new HomeFragment();
        graphFragment = new GraphFragment();
        alertsFragment = new AlertsFragment();

        fragmentMap.put(R.id.action_screen1, homeFragment);
        fragmentMap.put(R.id.action_screen2, graphFragment);
        fragmentMap.put(R.id.action_screen3, alertsFragment);

        // Set the default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = fragmentMap.get(item.getItemId());

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        hideSystemUI();
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        WindowInsetsController insetsController = decorView.getWindowInsetsController();

        if (insetsController != null) {
            insetsController.hide(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        }
    }
}