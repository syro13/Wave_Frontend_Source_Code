package com.example.wave;

import com.google.gson.annotations.SerializedName;

public class UserInfo {

    @SerializedName("fullname")
    private String fullName;

    public String getFullName() {
        return fullName;
    }

}
