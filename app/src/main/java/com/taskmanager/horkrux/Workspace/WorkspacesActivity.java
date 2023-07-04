package com.taskmanager.horkrux.Workspace;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.taskmanager.horkrux.CommonUtils;
import com.taskmanager.horkrux.Models.Count;
import com.taskmanager.horkrux.Models.Workspace;
import com.taskmanager.horkrux.R;
import com.taskmanager.horkrux.databinding.CreateWorkspaceBinding;
import com.taskmanager.horkrux.databinding.NavHeaderMainBinding;
import com.taskmanager.horkrux.databinding.WorkspaceListBinding;

import java.util.ArrayList;

public class WorkspacesActivity extends AppCompatActivity {
    private WorkspaceListBinding binding;
    public DrawerLayout drawerLayout;
    private NavHeaderMainBinding navHeaderMainBinding;
    private NavigationView navigationView;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private String USER_PATH;
    private String WORKSPACE_PATH;
    public static Count count;
    private ArrayList<Workspace> workspaces;
    private WorkspaceAdapter workspaceAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = WorkspaceListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        count = new Count(0, 0, 0, 0);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        drawerLayout = findViewById(R.id.my_drawer_layout);
        USER_PATH = "Users/" + auth.getUid() + "/";
        WORKSPACE_PATH = "workspaces/";
        workspaces = new ArrayList<>();
        workspaceAdapter = new WorkspaceAdapter(this, workspaces, "");
        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAndRemoveTask();
            }
        });
        binding.addWorkSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogCreateWorkspace();
            }
        });
        binding.workspaceList.setAdapter(workspaceAdapter);
        binding.workspaceList.setLayoutManager(new LinearLayoutManager(binding.workspaceList.getContext()));
        loadWorkspace();
    }

    private void showDialogCreateWorkspace(){
        Dialog dialog = new Dialog(this);
        CreateWorkspaceBinding createWorkspaceBinding = CreateWorkspaceBinding.inflate(getLayoutInflater());
        dialog.setContentView(createWorkspaceBinding.getRoot());
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int dialogWidth = (int) (screenWidth * 0.9);
        int dialogHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        Window window = dialog.getWindow();
        window.setLayout(dialogWidth, dialogHeight);
        createWorkspaceBinding.btnCreateWorkspace.setEnabled(false);

        createWorkspaceBinding.nameSpace.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
               createWorkspaceBinding.btnCreateWorkspace.setEnabled(charSequence.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        createWorkspaceBinding.btnCreateWorkspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createWorkspace(String.valueOf(createWorkspaceBinding.nameSpace.getText()));
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void createWorkspace(String namespace) {
        Workspace workspace = new Workspace(namespace, CommonUtils.generateId(), auth.getUid());
        ArrayList<String> admins = new ArrayList<String>();
        database.getReference().child(USER_PATH).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                admins.clear();
                admins.add(auth.getUid());
                workspace.setAdmins(admins);
                String path = WORKSPACE_PATH + workspace.getId();
                database.getReference().child(path).setValue(workspace)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(WorkspacesActivity.this, "Suscessfull", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(WorkspacesActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadWorkspace() {
        database.getReference().child(WORKSPACE_PATH)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        workspaces.clear();
                        for(DataSnapshot snap : snapshot.getChildren()) {
                            Workspace workspace = snap.getValue(Workspace.class);
                            assert workspace != null;
                            if(workspace.getCreatedBy().equals(auth.getUid())) {
                                workspaces.add(workspace);
                            }

                            if((workspace.getMembers() != null) && workspace.getMembers().contains(auth.getUid())){
                                workspaces.add(workspace);
                            }
                        }
                        workspaceAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
