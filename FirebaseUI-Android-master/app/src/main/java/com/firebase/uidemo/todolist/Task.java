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

    public Task(){

    }

    public Task(String title, String notes, String taskid){
        taskTitle = (title == null) ? "" : title;
        this.notes = (notes == null) ? "" : notes;
        this.taskid = taskid;
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

    public String getEmail(){
        return notes;
    }

    public String getUid(){
        return id;
    }

    public String getTaskid() {return taskid;}

    public boolean checkUpdates (String title, String notes) {
        Log.d("TASK", "title: " + this.taskTitle + "notes: " + this.notes);
        if(!this.taskTitle.equals(title) || !this.notes.equals(notes))
            return true;
        return false;
    }
}
