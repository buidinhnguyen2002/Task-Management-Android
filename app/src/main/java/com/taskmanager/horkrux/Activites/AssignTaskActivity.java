package com.taskmanager.horkrux.Activites;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.taskmanager.horkrux.Adapters.UserAdapter;
import com.taskmanager.horkrux.CommonUtils;
import com.taskmanager.horkrux.Models.Task;
import com.taskmanager.horkrux.Models.Users;
import com.taskmanager.horkrux.Models.Workspace;
import com.taskmanager.horkrux.R;
import com.taskmanager.horkrux.databinding.ActivityAssignTaskBinding;

import java.util.ArrayList;

public class AssignTaskActivity extends AppCompatActivity {
    final Context context = AssignTaskActivity.this;
    final String USER_TASK_PATH = "workspaces/";
    final String PROGRESS_MESSAGE = "Assigning Task";

    private boolean isTaskSubmitted = false;
    private ActivityAssignTaskBinding binding;
    private Task task;
    private FirebaseDatabase database;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    public static int count = 0;

    private Task selectedTask;
    private MaterialDatePicker.Builder materialDateBuilder;
    private MaterialDatePicker startDatePicker;
    private MaterialDatePicker dueDatePicker;
    private ListPopupWindow userList;
    private UserAdapter adapter;
    private ArrayAdapter userListAdapter;
    private ArrayList<Users> assignedList;
    private ArrayList<String> assignedId;
    public static ArrayList<Users> items;
    public static ArrayList<String> showingItems;
    private boolean isEdit = false;
    private String workspaceId;

    private ProgressDialog progressDialog;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAssignTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        selectedTask = (Task) getIntent().getSerializableExtra("selectedTask");
        workspaceId = getIntent().getSerializableExtra("workspaceId").toString();
        if (selectedTask != null) {
            isEdit = true;
        }

        initTaskUtils();
        if (isEdit) {
            setTasKUsingSelectedTask();
        }
        loadUsers();
    }

    private void setTasKUsingSelectedTask() {
        binding.titleTask.setText(selectedTask.getTaskTitle());
        assignedId.addAll(selectedTask.getGrpTask());
        binding.taskTitle.setText(selectedTask.getTaskTitle());
        binding.taskDescription.setText(selectedTask.getTaskDescription());
        binding.startDate.setText(selectedTask.getTaskAssigned());
        binding.dueDate.setText(selectedTask.getTaskDeadline());
        binding.actionTask.setText("Update Task");
        //setting priority
        if (selectedTask.getTaskPriority().equals(Task.LOW)) {
            binding.lowPriority.setChecked(true);
        }
        if (selectedTask.getTaskPriority().equals(Task.HIGH)) {
            binding.highPriority.setChecked(true);
        }
        if (selectedTask.getTaskPriority().equals(Task.MEDIUM)) {
            binding.mediumPriority.setChecked(true);
        }

        //setting status
        if (selectedTask.getTaskStatus().equals(Task.TODO)) {
            binding.todoStatus.setChecked(true);
        }
        if (selectedTask.getTaskStatus().equals(Task.IN_PROGRESS)) {
            binding.inProgressStatus.setChecked(true);
        }
        if (selectedTask.getTaskStatus().equals(Task.DONE)) {
            binding.doneStatus.setChecked(true);
        }


        adapter.notifyDataSetChanged();
    }


    private void initTaskUtils() {
        //init database variables
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        database = FirebaseDatabase.getInstance();

        //init variables
        materialDateBuilder = MaterialDatePicker.Builder.datePicker();
        materialDateBuilder.setTitleText("SELECT A DATE");
        startDatePicker = materialDateBuilder.build();
        dueDatePicker = materialDateBuilder.build();
        assignedList = new ArrayList<>();
        assignedId = new ArrayList<>();
        items = new ArrayList<>();
        showingItems = new ArrayList<>();
        userList = new ListPopupWindow(context);

        if (isEdit) {
            adapter = new UserAdapter(AssignTaskActivity.this, assignedList, selectedTask.getTaskID());
            binding.deleteTask.setOnClickListener(deleteTask);
        } else {
            binding.deleteTask.setVisibility(View.GONE);
            adapter = new UserAdapter(AssignTaskActivity.this, assignedList, null);
        }

        userListAdapter = new ArrayAdapter(context, R.layout.user_list, showingItems);
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(PROGRESS_MESSAGE);

        //date picker
        binding.startDate.setOnClickListener(startDatePick);
        binding.dueDate.setOnClickListener(dueDatePick);
        dueDatePicker.addOnPositiveButtonClickListener(dueDateOnPositive);
        startDatePicker.addOnPositiveButtonClickListener(startDateOnPositive);


        //action on add button
        binding.assignTaskToUserBtn.setOnClickListener(assignUserToTask);
        binding.taskAssignTo.setLayoutManager(new GridLayoutManager(AssignTaskActivity.this, 2));
        binding.taskAssignTo.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        binding.todoStatus.setChecked(true);

        //action on submit task btn
        binding.submitTask.setOnClickListener(assignTask);

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAndRemoveTask();
            }
        });

    }

    View.OnClickListener deleteTask = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            deleteTask();
            finishAndRemoveTask();
        }
    };


    // action on submit task
    View.OnClickListener assignTask = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (binding.taskTitle.getText().toString().isEmpty() || binding.taskDescription.getText().toString().isEmpty()) {
                Toast.makeText(context, "Please fill all details", Toast.LENGTH_SHORT).show();
                return;

            }

            if (binding.priorityGroup.getCheckedChipId() == -1) {
                Toast.makeText(context, "Please select priority", Toast.LENGTH_SHORT).show();
                return;
            }

            if(binding.statusGroupAdmin.getCheckedChipId() == -1){
                Toast.makeText(context, "Please select status", Toast.LENGTH_SHORT).show();
                return;
            }

            if (assignedList.isEmpty()) {
                Toast.makeText(context, "Please Assign task to someone", Toast.LENGTH_SHORT).show();
                return;
            }

            if (binding.startDate.getText().toString().equals("Pick Start Date ....")) {
                Toast.makeText(context, "Please Pick start date", Toast.LENGTH_SHORT).show();
                return;

            }

            if (binding.dueDate.getText().toString().equals("Pick Due Date ....")) {
                Toast.makeText(context, "Please Pick a due date", Toast.LENGTH_SHORT).show();
                return;

            }


            //assign task values
            task = new Task();
            if (isEdit) {
                task.setTaskID(selectedTask.getTaskID());
            } else {
                task.setTaskID(CommonUtils.generateId());
            }
            task.setTaskTitle(binding.taskTitle.getText().toString());
            task.setTaskDescription(binding.taskDescription.getText().toString());
            task.setTaskPriority(getSelectedPriority());
            task.setGrpTask(assignedId);
            task.setTaskAssigned(binding.startDate.getText().toString());
            task.setTaskDeadline(binding.dueDate.getText().toString());
            task.setTaskStatus(getStatus());
            task.setWorkspaceId(workspaceId);

            //add task to database
            AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
            builder1.setMessage("Do you really want to Assign this task");
            builder1.setCancelable(true);
            builder1.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            progressDialog.show();
                            addTaskToDatabase();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                CommonUtils.sendNotificationToUser(task, context);
                            }
                            isTaskSubmitted = true;
                        }
                    });
            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();


        }
    };

    //add data to database
    synchronized private void addTaskToDatabase() {
            database.getReference()
                    .child("tasks/" + task.getTaskID())
                    .setValue(task)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                            if(!isEdit) {
                                resetAllInputs();
                            }else {
                                assignedList.clear();
                                loadUsers();
                            }
                            progressDialog.dismiss();
                        }
                    });

    }

    void resetAllInputs() {
        binding.taskTitle.setText(null);
        binding.taskDescription.setText(null);
        binding.highPriority.setChecked(false);
        binding.mediumPriority.setChecked(false);
        binding.lowPriority.setChecked(false);
        assignedList.clear();
        assignedId.clear();
        adapter.notifyDataSetChanged();
        binding.startDate.setText(null);
        binding.dueDate.setText(null);
        binding.taskTitle.requestFocus();
        loadUsers();
        count = 0;


    }

    //action when click on + button
    View.OnClickListener assignUserToTask = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            userList.setHeight(ListPopupWindow.WRAP_CONTENT);
            userList.setWidth(600);
            userList.setAnchorView(v);
            userList.setAdapter(userListAdapter);

            userListAdapter.notifyDataSetChanged();
            userList.setOnItemClickListener(userClicked);
            userList.show();

        }
    };

    //load users from database
    synchronized private void loadUsers() {
        String path = "workspaces/" + workspaceId;
        items.clear();
        showingItems.clear();

        database.getReference().child(path).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue(Workspace.class) == null) {
                    finishAndRemoveTask();
                    return;
                }
                try {
                    addUser(snapshot);
                } catch (Exception e) {
                    Log.d("TAG", "onDataChange: ");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //action when user from list is clicked
    AdapterView.OnItemClickListener userClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            assignedList.clear();
            assignedId.clear();
            assignedId.add(items.get(position).getFireuserid());
            items.remove(position);
            showingItems.remove(position);
            userList.dismiss();
            adapter.notifyDataSetChanged();
            loadUsers();
        }
    };

    //action when pick start date is clicked
    View.OnClickListener startDatePick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        }
    };


    //action when pick due date is clicked
    View.OnClickListener dueDatePick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dueDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        }
    };

    // shows the calender
    MaterialPickerOnPositiveButtonClickListener startDateOnPositive = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            binding.startDate.setText(startDatePicker.getHeaderText());
        }
    };


    //shows the calender
    MaterialPickerOnPositiveButtonClickListener dueDateOnPositive = new MaterialPickerOnPositiveButtonClickListener() {
        @Override
        public void onPositiveButtonClick(Object selection) {
            binding.dueDate.setText(dueDatePicker.getHeaderText());
        }
    };


    //generate task ID



    // for getting the selected priority
    private String getSelectedPriority() {
        if (binding.highPriority.isChecked()) {
            return "high";
        } else if (binding.mediumPriority.isChecked()) {
            return "medium";
        } else {
            return "low";
        }
    }

    private String getStatus() {
        if(binding.todoStatus.isChecked()) {
            return Task.TODO;
        }else if(binding.inProgressStatus.isChecked()) {
            return Task.IN_PROGRESS;
        }else {
            return Task.DONE;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    private void deleteTask() {
        database.getReference().child("workspaces/" + workspaceId + "/tasks/" + selectedTask.getTaskID())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        snapshot.getRef().removeValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        database.getReference().child("tasks/" + selectedTask.getTaskID())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        snapshot.getRef().removeValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    synchronized private void addUser (DataSnapshot snapshot) {
        Workspace workspace = snapshot.getValue(Workspace.class);
        ArrayList<String> members = new ArrayList<>();
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
                            if((assignedId.size() <= 0) || !(user.getFireuserid().equals(assignedId.get(0)))) {
                                for(Users userItem : items) {
                                    if(userItem.getFireuserid().equals(user.getFireuserid())) {
                                       return;
                                    }
                                }
                                items.add(user);
                                if(!showingItems.contains(user.getUserName())) {
                                    showingItems.add(user.getUserName());
                                }
                            }else {
                                assignedList.add(user);
                                adapter.notifyDataSetChanged();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }
}