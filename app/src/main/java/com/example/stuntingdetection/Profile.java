package com.example.stuntingdetection;

public class Profile {

    private String username;
    private String height;
    private String age;
    private String status;
    private String gender;
    private String profileId;
    private String malnutritionStatus;

    // Empty constructor for Firestore
    public Profile() {
    }

    public Profile(String username, String height, String age, String status, String profileId, String gender, String malnutritionStatus) {
        this.username = username;
        this.height = height;
        this.age = age;
        this.status = status;
        this.profileId = profileId;
        this.gender = gender;
        this.malnutritionStatus = malnutritionStatus;
    }

    // Getter and Setter methods

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getGender() {
        return gender;
    }

    public void setMalnutritionStatus(String malnutritionStatus){this.malnutritionStatus=malnutritionStatus;
    }
    public String getMalnutritionStatus(){
        return malnutritionStatus;
    }
}

