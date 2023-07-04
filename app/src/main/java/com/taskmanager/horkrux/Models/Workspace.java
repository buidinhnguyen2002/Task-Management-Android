package com.taskmanager.horkrux.Models;

import java.util.ArrayList;

public class Workspace {
    private String id;
    private String name;
    private ArrayList<String> members;
    private ArrayList<String> admins;
    private ArrayList<String> tasks;
    private String createdBy;
    public Workspace() {
    }

    public Workspace(String name, String id, String createdBy) {
        this.name = name;
        this.id = id;
        this.createdBy = createdBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public ArrayList<String> getAdmins() {
        return admins;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public void setAdmins(ArrayList<String> admins) {
        this.admins = admins;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ArrayList<String> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<String> tasks) {
        this.tasks = tasks;
    }
}
