package com.firebase.uidemo.database;

public class Chat implements Comparable<Chat> {
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Chat)) return false;
        Chat chat = (Chat) o;
        return chat.mName.equals(this.mName) && chat.mMessage.equals(this.mMessage)
                && chat.mUid.equals(this.mUid) && chat.mTimeStamp == this.mTimeStamp;
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

    public void setTimeStamp(long timeStamp) { mTimeStamp = timeStamp; }

    @Override
    public int compareTo(Chat chat) {
        return (int) (this.mTimeStamp - chat.mTimeStamp);
    }
}
