package com.taskmanager.horkrux.Activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.taskmanager.horkrux.Authentication.LoginActivity;
import com.taskmanager.horkrux.Models.Count;
import com.taskmanager.horkrux.Models.Users;
import com.taskmanager.horkrux.R;
import com.taskmanager.horkrux.Workspace.WorkspacesActivity;
import com.taskmanager.horkrux.databinding.ActivityMainBinding;
import com.taskmanager.horkrux.databinding.NavHeaderMainBinding;
import com.taskmanager.horkrux.ui.home.HomeFragment;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    public static Count count;
    private HomeFragment homeFragment;
    private FragmentTransaction fragmentTransaction;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private NavHeaderMainBinding navHeaderMainBinding;
    private NavigationView navigationView;
    private String USER_PATH;

    @Override
    protected void onStart() {
        super.onStart();

        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        homeFragment = new HomeFragment();

        fragmentTransaction.replace(R.id.currentActivity, homeFragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        count = new Count(0, 0, 0, 0);

        USER_PATH = "Users/" + auth.getUid() + "/";

        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        binding.navDrawMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        binding.userNotifications.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), ViewNotificationsActivity.class));
        });
        setNavigation();

    }

    private void setNavigation() {
        navigationView = findViewById(R.id.navView);
        navHeaderMainBinding = NavHeaderMainBinding.bind(navigationView.getHeaderView(0));

        navHeaderMainBinding.cancelNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        database.getReference().child(USER_PATH).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                navHeaderMainBinding.loggedInUserName.setText(user.getUserName());
                navHeaderMainBinding.loggedInUserMail.setText(user.getUserEmail());
                if(!user.getUserProfile().equals(Users.NO_PROFILE)) {
                    Glide.with(getApplicationContext()).load(user.getUserProfile()).placeholder(R.drawable.place_holder).into(navHeaderMainBinding.loggedInUserProfile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                if (item.getItemId() == R.id.nav_home) {
                    drawerLayout.closeDrawers();
                    fragment = new HomeFragment();
                    return true;
                }
                if (item.getItemId() == R.id.nav_profile) {
                    Intent profileIntent = new Intent(MainActivity.this, Profile.class);
                    startActivity(profileIntent);
                    drawerLayout.closeDrawers();
                    return false;
                }
                if (item.getItemId() == R.id.nav_sign_out) {
                    auth.signOut();
                    Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finishAffinity();
                    return false;
                }

                if(item.getItemId() == R.id.nav_workspaces) {
                    startActivity(new Intent(MainActivity.this, WorkspacesActivity.class));
                    return false;
                }

                fragmentTransaction.replace(R.id.currentActivity, fragment);
                fragmentTransaction.commit();
                drawerLayout.closeDrawers();

                return true;
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        actionBarDrawerToggle.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }
}