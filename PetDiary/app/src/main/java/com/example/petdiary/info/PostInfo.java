package com.example.petdiary.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostInfo {

    private String uid;
    private String email;
    private String category;
    private String petsID;
    private String imageUrl1;
    private String imageUrl2;
    private String imageUrl3;
    private String imageUrl4;
    private String imageUrl5;
    private String content;
    private ArrayList<String> hashTag = new ArrayList<String>();
    private String date;
    private String nickName;
    private int favoriteCount = 0;
    private Map<String, Boolean> favorites = new HashMap<>();
    private Map<String, Comment> comments;

    public static class Comment {

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

    public String getPetsID() { return petsID; }
    public void setPetsID(String petsID) { this.petsID = petsID; }

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

    public String getImageUrl4() {
        return imageUrl4;
    }
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

    public String getNickName() {
        return nickName;
    }
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(int favoriteCount) { this.favoriteCount = favoriteCount; }


    @Override
    public String toString() {
        return "uid = " + uid + " , email = " + email;
    }
}
