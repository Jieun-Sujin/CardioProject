package com.jieun.cardiocare;

public class AEDItem {
    public String org; //설치 기관명
    public String buildPlace; //설치 장소
    public String clerkTel; //전화번호
    public String buildAddress; //상세주소
    public String distance; //거리
    public String wgs84Lat; //위도
    public String wgs84Lon; //경도


    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getBuildPlace() {
        return buildPlace;
    }

    public void setBuildPlace(String buildPlace) {
        this.buildPlace = buildPlace;
    }

    public String getClerkTel() {
        return clerkTel;
    }

    public void setClerkTel(String clerkTel) {
        this.clerkTel = clerkTel;
    }

    public String getBuildAddress() {
        return buildAddress;
    }

    public void setBuildAddress(String buildAddress) {
        this.buildAddress = buildAddress;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getWgs84Lat() {
        return wgs84Lat;
    }

    public void setWgs84Lat(String wgs84Lat) {
        this.wgs84Lat = wgs84Lat;
    }

    public String getWgs84Lon() {
        return wgs84Lon;
    }

    public void setWgs84Lon(String wgs84Lon) {
        this.wgs84Lon = wgs84Lon;
    }
}
