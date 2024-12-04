package com.example.eventsync_message;

public class Contacts {

    public String name, status;

    public Contacts(){

    }

    public Contacts(String name, String status){
        this.name = name;
        this.status = status;

    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
