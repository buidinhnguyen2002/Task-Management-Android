package com.taskmanager.horkrux.AdminPanel;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.taskmanager.horkrux.Models.Users;
import com.taskmanager.horkrux.R;
import com.taskmanager.horkrux.databinding.ActivityUserTasksBinding;
import com.taskmanager.horkrux.ui.home.HomeFragment;

public class UserTasksActivity extends AppCompatActivity {

    final private Context context = UserTasksActivity.this;
    private ActivityUserTasksBinding binding;
    private Users selectedUser;
    private HomeFragment homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserTasksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        selectedUser = (Users) getIntent().getSerializableExtra("selectedUser");
        homeFragment = new HomeFragment(selectedUser, getIntent().getSerializableExtra("workspaceId").toString());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.userTaskFrame, homeFragment);
        transaction.commit();


    }


}