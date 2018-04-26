package com.amanachintyanikhil.blogapp;

/**
 * Created by Home on 19-Mar-18.
 */

public class Comment
{
    String username;
    String comment;
    String image;
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public Comment(String username, String comment) {
        this.username = username;
        this.comment = comment;
    }

    public Comment() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
