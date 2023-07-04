package com.taskmanager.horkrux.AdminPanel;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.taskmanager.horkrux.Models.Users;
import com.taskmanager.horkrux.Models.Workspace;
import com.taskmanager.horkrux.databinding.ActivityTeamMemberListBinding;

import java.util.ArrayList;

public class TeamMemberList extends AppCompatActivity {

    private ActivityTeamMemberListBinding binding;
    private final Context context = TeamMemberList.this;
    private UserInviteAdapter adapter;
    private FirebaseDatabase database;
    private ArrayList<Users> users;
    String workspaceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTeamMemberListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        workspaceId = getIntent().getStringExtra("workspaceId");

        users = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        adapter = new UserInviteAdapter(context, users, workspaceId);

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finishAndRemoveTask();
            }
        });
        binding.inviteUser.setOnClickListener(inviteUser);

        binding.adminUserRecylerView.setAdapter(adapter);
        binding.adminUserRecylerView.setLayoutManager(new LinearLayoutManager(binding.adminUserRecylerView.getContext()));
        loadUsers();

    }

    View.OnClickListener inviteUser = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String email = binding.emailInvite.getText().toString();
            database.getReference().child("Users")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean invited = false;
                            for (DataSnapshot snap : snapshot.getChildren()) {
                                Users user = snap.getValue(Users.class);
                                if(user.getUserEmail().equals(email)) {
                                    addMemberToWorkspace(user);
                                    invited = true;
                                    break;
                                }
                            }
                            if(!invited) {
                                Toast.makeText(context, "Email is not register", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    };

    private void loadUsers() {
        String path = "workspaces/" + workspaceId;
        binding.progressBar.setVisibility(View.VISIBLE);
        database.getReference().child(path).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                adapter.notifyDataSetChanged();
                Workspace workspace = snapshot.getValue(Workspace.class);
                ArrayList<String> members = new ArrayList<>();
                if(workspace == null) {
                    finishAndRemoveTask();
                    return;
                }
                members.addAll(workspace.getAdmins());
                if(workspace.getMembers() != null) {
                    members.addAll(workspace.getMembers());
                }
                for(String userId : members) {
                    database.getReference().child("Users/" + userId)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Users user = snapshot.getValue(Users.class);
                                    users.add(user);
                                    adapter.notifyDataSetChanged();
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                }

                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addMemberToWorkspace(Users user) {
        database.getReference().child("workspaces/" + workspaceId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Workspace workspace = snapshot.getValue(Workspace.class);
                        int posMb = 0;
                        if(workspace == null) {
                            finishAndRemoveTask();
                            return;
                        }
                        if(workspace.getMembers() != null) {
                            posMb = workspace.getMembers().size();
                            for(String id : workspace.getMembers()) {
                                if(id.equals(user.getFireuserid())) {
                                    Toast.makeText(context, "User is invited", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        }
                        if(workspace.getCreatedBy().equals(user.getFireuserid())) {
                            Toast.makeText(context, "User is invited", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        database.getReference().child("workspaces/" + workspaceId + "/members/" + posMb)
                                .setValue(user.getFireuserid())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Toast.makeText(context, "Invite successful", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

}