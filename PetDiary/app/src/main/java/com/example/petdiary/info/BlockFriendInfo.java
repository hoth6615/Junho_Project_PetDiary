package com.example.petdiary.info;

public class BlockFriendInfo {

    private String friendUid;

    public BlockFriendInfo(){
    }

    public BlockFriendInfo(String friendUid){
        this.friendUid = friendUid;
    }

    public String getFriendUid(){
        return this.friendUid;
    }
    public void setFriendUid(String friendUid){ this.friendUid = friendUid; }

}
