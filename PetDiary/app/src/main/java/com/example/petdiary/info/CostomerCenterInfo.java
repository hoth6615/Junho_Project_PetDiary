package com.example.petdiary.info;

public class CostomerCenterInfo {

    private String email;
    private String category;
    private String contents;
    private String uid;
    private String date;

    public CostomerCenterInfo(){
    }

    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }

    public String getCategory(){
        return this.category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getContents(){
        return this.contents;
    }
    public void setContents(String contents){
        this.contents = contents;
    }

    public String getUid(){
        return this.uid;
    }
    public void setUid(String uid){ this.uid = uid; }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

}
