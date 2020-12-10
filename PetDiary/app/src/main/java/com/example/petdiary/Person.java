package com.example.petdiary;

public class Person {

    String nickname;
    String uid;

    public Person(){

    }
    public Person(String nickname){
        this.nickname = nickname;
    }
    public Person(String nickname, String uid){
        this.uid = uid;
        this.nickname = nickname;
    }
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
