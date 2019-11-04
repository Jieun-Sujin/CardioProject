package com.jieun.cardiocare;

import java.io.Serializable;

public class HeartUser implements Serializable {
    private float bpm;
    private int status;
    private String date;

    public float getBpm() {
        return bpm;
    }

    public void setBpm(float bpm) {
        this.bpm = bpm;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
