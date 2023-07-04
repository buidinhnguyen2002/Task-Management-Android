package com.taskmanager.horkrux.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.taskmanager.horkrux.Activites.AssignTaskActivity;
import com.taskmanager.horkrux.Models.Task;
import com.taskmanager.horkrux.Models.Users;
import com.taskmanager.horkrux.R;
import com.taskmanager.horkrux.databinding.TaskItemBinding;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private Context context;
    private ArrayList<Task> tasks;
    private final String from;
    private String workspaceId;

    public TaskAdapter(ArrayList<Task> tasks, String from, String workspaceId) {
        this.tasks = tasks;
        this.from = from;
        this.workspaceId = workspaceId;
    }

    @NonNull
    @Override
    public TaskAdapter.TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        return new TaskAdapter.TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskAdapter.TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        String priority = task.getTaskPriority();
        if (priority.equals(Task.LOW)) {
            holder.binding.taskItem.setCardBackgroundColor(context.getResources().getColor(R.color.low_green));
            holder.binding.startingDate.setTextColor(context.getResources().getColor(R.color.dark_green));
            holder.binding.deadlineDate.setTextColor(context.getResources().getColor(R.color.dark_green));
            holder.binding.priorityShow.setChipBackgroundColor(ColorStateList.valueOf(context.getResources().getColor(R.color.dark_green)));
            holder.binding.priorityShow.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.white)));
            holder.binding.userTaskTitle.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.title_text)));
            holder.binding.userTaskDescription.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.desc_text)));
        } else if (priority.equals(Task.MEDIUM)) {
            holder.binding.taskItem.setCardBackgroundColor(context.getResources().getColor(R.color.low_yellow));
            holder.binding.startingDate.setTextColor(context.getResources().getColor(R.color.dark_yellow));
            holder.binding.deadlineDate.setTextColor(context.getResources().getColor(R.color.dark_yellow));
            holder.binding.priorityShow.setChipBackgroundColor(ColorStateList.valueOf(context.getResources().getColor(R.color.dark_yellow)));
            holder.binding.priorityShow.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.white)));
            holder.binding.userTaskTitle.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.title_text)));
            holder.binding.userTaskDescription.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.desc_text)));
        } else {
            holder.binding.taskItem.setCardBackgroundColor(context.getResources().getColor(R.color.low_red));
            holder.binding.startingDate.setTextColor(context.getResources().getColor(R.color.dark_red));
            holder.binding.deadlineDate.setTextColor(context.getResources().getColor(R.color.dark_red));
            holder.binding.priorityShow.setChipBackgroundColor(ColorStateList.valueOf(context.getResources().getColor(R.color.dark_red)));
            holder.binding.priorityShow.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.white)));
            holder.binding.userTaskTitle.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.title_text)));
            holder.binding.userTaskDescription.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.desc_text)));
        }


        holder.binding.userTaskTitle.setText(tasks.get(position).getTaskTitle());
        holder.binding.userTaskDescription.setText(tasks.get(position).getTaskDescription());
        holder.binding.startingDate.setText(tasks.get(position).getTaskAssigned());
        holder.binding.deadlineDate.setText(tasks.get(position).getTaskDeadline());
        holder.binding.priorityShow.setText(tasks.get(position).getTaskPriority());
        // load avatar and set circle avatar

        FirebaseDatabase.getInstance().getReference("Users/" + task.getGrpTask()
                .get(0))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users assignee = snapshot.getValue(Users.class);
                        if(assignee.getUserProfile().equals("No Profile")) {
                            holder.binding.textLabel.setText(assignee.getUserName().substring(0, 1).toUpperCase());
                            holder.binding.avatarAssignee.setVisibility(View.GONE);
                        }else {
                            holder.binding.avatarAssignee.setVisibility(View.VISIBLE);
                            Glide.with(context).load(assignee.getUserProfile()).apply(RequestOptions.circleCropTransform()).into(holder.binding.avatarImage);
                            holder.binding.textAssignee.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
        holder.binding.taskItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AssignTaskActivity.class);
                if(workspaceId != null) {
                    intent.putExtra("workspaceId", workspaceId);
                }else {
                    intent.putExtra("workspaceId", task.getWorkspaceId());
                }
                intent.putExtra("selectedTask", task);
                context.startActivity(intent);
            }
        });
    }


    public void applyFilter(ArrayList<Task> filteredTask) {
        tasks = filteredTask;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TaskItemBinding binding;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = binding.bind(itemView);
        }
    }
}
