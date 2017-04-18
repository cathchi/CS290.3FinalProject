package com.firebase.uidemo.chat;

import java.util.ArrayList;

public class Chat implements Comparable<Chat> {
    private String mName;
    private String mMessage;
    private String mUID;
    private String mRecipientUID;
    private String mRecipientName;
    private ArrayList<String> mRecipientUIDs;
    private Long mTimeStamp;

    public Chat() {
        // Needed for Firebase
    }

//    public Chat(String name, String message, String uid) {
//        mName = name;
//        mMessage = message;
//        mUid = uid;
//    }

//    public Chat(String name, String message, String uid, Long timestamp) {
//        mName = name;
//        mMessage = message;
//        mUID = uid;
//        mTimeStamp = timestamp;
//    }

    public Chat(String name, String rname, String message, String uid, String ruid, Long timestamp) {
        mName = name;
        mRecipientName = rname;
        mMessage = message;
        mUID = uid;
        mRecipientUID = ruid;
        mTimeStamp = timestamp;
    }

//    public Chat(String name, String message, String uid, ArrayList<String> ruid, Long timestamp) {
//        mName = name;
//        mMessage = message;
//        mUID = uid;
//        mRecipientUIDs = ruid;
//        mTimeStamp = timestamp;
//    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Chat)) return false;
        Chat chat = (Chat) o;
        return chat.mName.equals(this.mName) && chat.mMessage.equals(this.mMessage) &&
                chat.mRecipientName.equals(this.mRecipientName)
                && chat.mRecipientUID.equals(this.mRecipientUID)
                && chat.mUID.equals(this.mUID) && chat.mTimeStamp.equals(this.mTimeStamp);
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getRName() { return mRecipientName; }

    public void setRName(String name) { mRecipientName = name; }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getUid() {
        return mUID;
    }

    public void setUid(String uid) {
        mUID = uid;
    }

    public String getRUID() { return mRecipientUID; }

    public void setRUID(String ruid) { mRecipientUID = ruid; }

//    public ArrayList<String> getRUIDs() { return mRecipientUIDs; }

    //public void setRUIDs(ArrayList<String> ruid) { mRecipientUIDs = ruid; }

    public Long getTimeStamp() { return mTimeStamp; }

    public void setTimeStamp(long timeStamp) { mTimeStamp = timeStamp; }

    @Override
    public int compareTo(Chat chat) {
        return (int) (this.mTimeStamp - chat.mTimeStamp);
    }
}
