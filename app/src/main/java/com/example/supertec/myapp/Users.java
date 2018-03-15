package com.example.supertec.myapp;

import java.security.PublicKey;

/**
 * Created by Supertec on 3/7/2018.
 */

public class Users {

    public String image;
    public String name;
    public String status;
    public String thumb_image;

    public Users(){

    }

    public Users(String image, String name, String status, String thumb_image) {
        this.image = image;
        this.name = name;
        this.status = status;
        this.thumb_image = thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
