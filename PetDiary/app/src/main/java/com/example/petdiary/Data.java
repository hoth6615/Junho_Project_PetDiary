package com.example.petdiary;

import java.util.ArrayList;

public class Data {

    private boolean bookmark;
    private boolean like;

    private String postID;
    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;
    private String imageUrl4;
    private String imageUrl5;
    private String content;
    private String nickName;
    private String uid;
    private String category;
    private String date;
    private String email;
    private ArrayList<String> hashTag = new ArrayList<String>();
    private int favoriteCount;


    public Data() { }
    public Data(String postID, String imageUrl1, String imageUrl2, String imageUrl3, String imageUrl4, String imageUrl5, String content, String nickName, String uid, String category, String date, String email) {
        this.postID = postID;
        this.imageUrl1 = imageUrl1;
        this.imageUrl2 = imageUrl2;
        this.imageUrl3 = imageUrl3;
        this.imageUrl4 = imageUrl4;
        this.imageUrl5 = imageUrl5;
        this.content = content;
        this.nickName = nickName;
        this.uid = uid;
        this.category = category;
        this.date = date;
        this.email = email;
    }

    public Boolean getBookmark() { return bookmark; }
    public void setBookmark(boolean bookmark) { this.bookmark = bookmark; }

    public Boolean getLike() { return like; }
    public void setLike(boolean like) { this.like = like; }

    public String getPostID() { return postID; }
    public void setPostID(String postID){ this.postID = postID; }

    public String getImageUrl1() {
        return imageUrl1;
    }
    public void setImageUrl1(String imageUrl1) {
        this.imageUrl1 = imageUrl1;
    }

    public String getImageUrl2() {
        return imageUrl2;
    }
    public void setImageUrl2(String imageUrl2) {
        this.imageUrl2 = imageUrl2;
    }

    public String getImageUrl3() {
        return imageUrl3;
    }
    public void setImageUrl3(String imageUrl3) {
        this.imageUrl3 = imageUrl3;
    }

    public String getImageUrl4() { return imageUrl4; }
    public void setImageUrl4(String imageUrl4) {
        this.imageUrl4 = imageUrl4;
    }

    public String getImageUrl5() {
        return imageUrl5;
    }
    public void setImageUrl5(String imageUrl5) {
        this.imageUrl5 = imageUrl5;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getNickName() {
        return nickName;
    }
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public ArrayList<String> getHashTag() {
        return hashTag;
    }
    public void setHashTag(ArrayList<String> hashTag) {
        this.hashTag = hashTag;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public int getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(int favoriteCount) { this.favoriteCount = favoriteCount; }
}