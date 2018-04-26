package com.amanachintyanikhil.blogapp;

/**
 * Created by Home on 28-Feb-18.
 */

public class Blog
{
    String image,title,desc,username;

    public Blog(String image, String title, String desc,String username) {
        this.image = image;
        this.title = title;
        this.desc = desc;
        this.username=username;
    }

    public Blog() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

