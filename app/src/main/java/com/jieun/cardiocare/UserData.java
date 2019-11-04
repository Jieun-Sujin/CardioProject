package com.jieun.cardiocare;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class UserData {

    private String name;
    private String age;
    private int gender;
    private float height;
    private float weight;
    private int ap_hi;
    private int ap_lo;
    private int cholesterol;
    private int smoke;
    private int alco;

    public UserData() {
    }

    public UserData(String name) {
        this.name = name;
    }
    public UserData(String name, int gender, String age, float height, float weight, int ap_hi, int ap_lo, int cholesterol, int smoke, int alco) {

        this.name = name;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.ap_hi = ap_hi;
        this.ap_lo = ap_lo;
        this.cholesterol = cholesterol;
        this.smoke = smoke;
        this.alco = alco;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getAp_hi() {
        return ap_hi;
    }

    public void setAp_hi(int ap_hi) {
        this.ap_hi = ap_hi;
    }

    public int getAp_lo() {
        return ap_lo;
    }

    public void setAp_lo(int ap_lo) {
        this.ap_lo = ap_lo;
    }

    public int getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(int cholesterol) {
        this.cholesterol = cholesterol;
    }

    public int getSmoke() {
        return smoke;
    }

    public void setSmoke(int smoke) {
        this.smoke = smoke;
    }

    public int getAlco() {
        return alco;
    }

    public void setAlco(int alco) {
        this.alco = alco;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("name", name);
        result.put("age", age);
        result.put("height", height);
        result.put("weight", weight);
        result.put("ap_hi", ap_hi);
        result.put("ap_lo", ap_lo);
        result.put("cholesterol", cholesterol);
        result.put("smoke", smoke);
        result.put("alco", alco);
        return result;
    }
}
