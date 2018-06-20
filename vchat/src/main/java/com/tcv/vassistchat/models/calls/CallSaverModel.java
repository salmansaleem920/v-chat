/*
 * *
 *  * Created by Salman Saleem on 6/21/18 12:43 AM
 *  * Copyright (c) 2018 . All rights reserved.
 *  * Last modified 6/21/18 12:32 AM
 *
 */

package com.tcv.vassistchat.models.calls;

/**
 * Created by Salman Saleem on 6/16/17.
 *
 *
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype :
 */

public class CallSaverModel {

    private int fromId;
    private int toId;
    private String date;
    private String duration;
    private String isVideo;

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public int getToId() {
        return toId;
    }

    public void setToId(int toId) {
        this.toId = toId;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getIsVideo() {
        return isVideo;
    }

    public void setIsVideo(String isVideo) {
        this.isVideo = isVideo;
    }
}
