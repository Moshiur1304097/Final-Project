package com.example.v.bluetooth;

public class Glucose {
    String date,time,value;
    Glucose(String date, String time, String value){
        this.date = date;
        this.time = time;
        this.value = value;
    }
    String getDate(){return date;}
    String getTime(){return time;}
    String getValue(){return value;}
}
