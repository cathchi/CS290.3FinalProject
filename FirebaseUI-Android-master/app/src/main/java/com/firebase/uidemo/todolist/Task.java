package com.firebase.uidemo.todolist;

/**
 * Created by JasmineLu on 4/15/17.
 */

public class Task {
    private String taskTitle;
    private String notes;
    private String id;

    public Task(){

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

    public String getEmail(){
        return notes;
    }

    public String getUid(){
        return id;
    }
}
