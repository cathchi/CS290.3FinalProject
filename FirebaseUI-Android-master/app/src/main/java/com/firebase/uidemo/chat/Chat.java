package com.firebase.uidemo.chat;

import java.util.ArrayList;

public class Chat implements Comparable<Chat> {
    private String mName;
    private String mMessage;
    private String mUID;
    private String mRecipientUID;
    private String mRecipientName;
    private Long mTimeStamp;
    private String mType;

    public Chat() {
        // Needed for Firebase
    }

    /**
     * Chat Object constructor
     * @param name = Name of sender
     * @param rname = Name of recipient
     * @param message = Message of chat
     * @param uid = ID of sender
     * @param ruid = ID of recipient
     * @param timestamp = Time message sent
     * @param type = Type of message
     */
    public Chat(String name, String rname, String message, String uid, String ruid, Long timestamp, String type) {
        mName = name;
        mRecipientName = rname;
        mMessage = message;
        mUID = uid;
        mRecipientUID = ruid;
        mTimeStamp = timestamp;
        mType = type;
    }

    /**
     *
     * @param o = Chat object for comparison
     * @return whether the Chat object being compared to is the same
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Chat)) return false;
        Chat chat = (Chat) o;
        return chat.mName.equals(this.mName) && chat.mMessage.equals(this.mMessage) &&
                chat.mRecipientName.equals(this.mRecipientName)
                && chat.mRecipientUID.equals(this.mRecipientUID)
                && chat.mUID.equals(this.mUID) && chat.mTimeStamp.equals(this.mTimeStamp)
                && chat.mType.equals(this.mType);
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

    public Long getTimeStamp() { return mTimeStamp; }

    public void setTimeStamp(long timeStamp) { mTimeStamp = timeStamp; }

    public String getType() { return mType; }

    public void setType(String type) { mType = type; }

    @Override
    public int compareTo(Chat chat) {
        return (int) (this.mTimeStamp - chat.mTimeStamp);
    }
}
