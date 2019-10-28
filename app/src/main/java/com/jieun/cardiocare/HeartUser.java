package com.jieun.cardiocare;

public class HeartUser {
    private float ppm;
    private int status;
    private String date;

    public HeartUser(float ppm, int status, String date) {
        this.ppm = ppm;
        this.status = status;
        this.date = date;
    }

    public float getPpm() {
        return ppm;
    }

    public void setPpm(float ppm) {
        this.ppm = ppm;
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
