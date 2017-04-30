package com.firebase.uidemo.todolist;

import java.util.ArrayList;

/**
 * Created by JasmineLu on 4/29/17.
 */

public class ListItem {
    private String listTitle;
    private ArrayList<String> users;
    private String myID;

    public ListItem(String title, ArrayList<String> team, String id){
        this.listTitle = (title == null) ? "": title;
        this.users = (team == null) ? new ArrayList<String>() : team;
        this.myID = (id == null) ? "": id;
    }

    public boolean isShared(){
        if(users.size() > 1)
            return true;
        else
            return false;
    }

    public String getListTitle(){
        return listTitle;
    }

    //Strings users together for use in display of App Activities
    public String getUserString(){
        String userString = "";
        for(String user: users)
            userString = userString + " " + user + ",";
        if(userString.length()==0)
            return userString;
        else {
            userString = userString.substring(0, userString.length() - 1);
            return userString;
        }
    }

    public String getMyID(){
        return myID;
    }
}
