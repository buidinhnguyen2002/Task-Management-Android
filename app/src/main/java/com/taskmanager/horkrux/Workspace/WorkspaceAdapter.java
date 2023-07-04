package com.taskmanager.horkrux.Workspace;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.taskmanager.horkrux.Models.Workspace;
import com.taskmanager.horkrux.R;
import com.taskmanager.horkrux.databinding.WorkspaceItemBinding;

import java.util.ArrayList;

public class WorkspaceAdapter extends RecyclerView.Adapter<WorkspaceAdapter.WorkspaceViewHolder> {
    private final Context context;
    private ArrayList<Workspace> workspaces;
    private final String from;

    public WorkspaceAdapter(Context context, ArrayList<Workspace> workspaces, String from) {
        this.context = context;
        this.workspaces = workspaces;
        this.from = from;
    }

    @NonNull
    @Override
    public WorkspaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workspace_item, parent, false);
        return new WorkspaceAdapter.WorkspaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkspaceViewHolder holder, int position) {
        Workspace workspace = workspaces.get(position);
        holder.binding.workspaceLabel.setText(workspace.getName().substring(0, 1));
        holder.binding.workspaceName.setText(workspace.getName());

        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, WorkspacesDetailActivity.class);
                intent.putExtra("currentId", workspace.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return workspaces.size();
    }

    public static class WorkspaceViewHolder extends RecyclerView.ViewHolder {
        WorkspaceItemBinding binding;

        public WorkspaceViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = binding.bind(itemView);
        }
    }
}
