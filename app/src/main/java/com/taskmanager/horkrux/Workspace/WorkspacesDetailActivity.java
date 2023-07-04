package com.taskmanager.horkrux.Workspace;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.taskmanager.horkrux.Activites.AssignTaskActivity;
import com.taskmanager.horkrux.AdminPanel.TeamMemberList;
import com.taskmanager.horkrux.Models.Task;
import com.taskmanager.horkrux.Models.Workspace;
import com.taskmanager.horkrux.R;
import com.taskmanager.horkrux.databinding.CreateWorkspaceBinding;
import com.taskmanager.horkrux.databinding.WorkspaceDetailBinding;
import com.taskmanager.horkrux.ui.home.HomeFragment;

public class WorkspacesDetailActivity extends AppCompatActivity {
    private WorkspaceDetailBinding binding ;
    public static String currentName;
    private String currentId;
    private HomeFragment homeFragment;
    private FragmentTransaction fragmentTransaction;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private PopupMenu menu;
    private boolean isNotAdmin = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = WorkspaceDetailBinding.inflate(getLayoutInflater());
        currentId = getIntent().getSerializableExtra("currentId").toString();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        setContentView(binding.getRoot());
        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { finish(); }
        });

        binding.workspaceAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu = new PopupMenu(WorkspacesDetailActivity.this, binding.workspaceAction);
                menu.getMenuInflater().inflate(R.menu.workspace_menu, menu.getMenu());
                if(isNotAdmin) {
                    menu.getMenu().getItem(0).setVisible(false);
                    menu.getMenu().getItem(2).setVisible(false);
                    menu.getMenu().getItem(3).setVisible(false);

                }
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getItemId() == R.id.create_task) {
                            Intent intent = new Intent(WorkspacesDetailActivity.this, AssignTaskActivity.class);
                            intent.putExtra("workspaceId", currentId);
                            startActivity(intent);
                            return true;
                        }
                        if(menuItem.getItemId() == R.id.invite_user) {
                            Intent intent = new Intent(WorkspacesDetailActivity.this, TeamMemberList.class);
                            intent.putExtra("workspaceId", currentId);
                            startActivity(intent);
                            return true;
                        }
                        if(menuItem.getItemId() == R.id.delete_workspace) {
                            deleteWorkspace();
                            finishAndRemoveTask();
                            return true;
                        }
                        if(menuItem.getItemId() == R.id.rename_workspace) {
                            dialogWorkspace();
                            menu.dismiss();
                            return true;
                        }
                        return false;
                    }
                });

                menu.show();
            }
        });
        loadWorkspace();
    }

    @Override
    protected void onStart() {
        super.onStart();
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        homeFragment = new HomeFragment(currentId, "");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.currentActivity, homeFragment);
        transaction.commit();
    }

    private void deleteWorkspace() {
        Thread thread = new Thread(() -> {
            database.getReference().child("workspaces/" + currentId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotWp) {
                            snapshotWp.getRef().removeValue();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        });
        thread.start();
        boolean isStop = false;
        while (thread.isAlive() || !isStop) {
            if(!thread.isAlive()){
                database.getReference().child("tasks")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot snap : snapshot.getChildren()) {
                                    if(snap.getValue(Task.class).getWorkspaceId().equals(currentId)) {
                                        snap.getRef().removeValue();
                                        break;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                isStop = true;
            }
        }
    }

    private void renameWorkspace(String newName) {
        database.getReference().child("workspaces/" + currentId + "/name").setValue(newName)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        Toast.makeText(WorkspacesDetailActivity.this, "Rename Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void dialogWorkspace() {
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

        createWorkspaceBinding.nameSpace.setText(binding.workspaceName.getText());
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
        createWorkspaceBinding.btnCreateWorkspace.setText("Rename");
        createWorkspaceBinding.btnCreateWorkspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renameWorkspace(String.valueOf(createWorkspaceBinding.nameSpace.getText()));
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void loadWorkspace() {
        database.getReference().child("workspaces/" + currentId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Workspace workspace = snapshot.getValue(Workspace.class);
                        if(workspace == null) {
                            finishAndRemoveTask();
                            return;
                        }
                        if(workspace.getCreatedBy().equals(auth.getUid())) {
                            isNotAdmin = false;
                        }
                        binding.workspaceName.setText(workspace.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
;