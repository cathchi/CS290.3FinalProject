package com.firebase.uidemo.database;

public class Chat {
    private String mName;
    private String mMessage;
    private String mUid;
    private Long mTimeStamp;

    public Chat() {
        // Needed for Firebase
    }

//    public Chat(String name, String message, String uid) {
//        mName = name;
//        mMessage = message;
//        mUid = uid;
//    }

    public Chat(String name, String message, String uid, Long timestamp) {
        mName = name;
        mMessage = message;
        mUid = uid;
        mTimeStamp = timestamp;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public Long getTimeStamp() { return mTimeStamp; }
}
