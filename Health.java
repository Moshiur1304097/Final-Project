package com.example.v.bluetooth;

public class Health {
    String bpm;
    String so2;
    String tem;
    Health(String bpm, String so2, String tem){
        this.bpm = bpm;
        this.so2 = so2;
        this.tem = tem;
    }
    String getBpm(){return bpm;}
    String getSo2(){return so2;}
    String getTem(){return tem;}
}
