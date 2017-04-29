package com.firebase.uidemo.todolist;

import android.util.Log;

/**
 * Created by JasmineLu on 4/15/17.
 */

public class Task {
    private String taskTitle;
    private String notes;
    private String id;
    private String taskid;
    private String location;

    public Task(){

    }

    public Task(String title, String notes, String taskid, String location){
        taskTitle = (title == null) ? "" : title;
        this.notes = (notes == null) ? "" : notes;
        this.taskid = taskid;
        this.location = (location == null) ? "" : location;
    }

    public void setTaskTitle(String title){
        this.taskTitle = title;
    }

    public void setNotes(String notesText){
        this.notes = notesText;
    }

    public void setId(String taskId){
        this.id = taskId;
    }

    public String getName(){
        return taskTitle;
    }

    public String getNotes() {
        return notes;
    }

    public String getUid(){
        return id;
    }

    public String getTaskid() {return taskid;}

    public boolean checkUpdates (String title, String notes) {
        if(!this.taskTitle.equals(title) || !this.notes.equals(notes))
            return true;
        return false;
    }


    public String getLocation() {return location; }

    public void setLocation(String location) {this.location= location; }
}
