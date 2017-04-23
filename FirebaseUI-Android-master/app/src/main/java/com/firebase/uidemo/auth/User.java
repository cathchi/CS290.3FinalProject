package com.firebase.uidemo.auth;

/**
 * Created by JordanBurton on 4/4/17.
 */

public class User {

    private String email;
    private String name;
    private String uid;
    private String image;

    public User(){

    }

    public void setEmail(String mail){
        this.email = mail;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setUid(String id){
        this.uid = id;
    }

    public void setImage(String image){
        this.image = image;
    }

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

    public String getUid(){
        return uid;
    }

    public String getImage(){
        return image;
    }

}
