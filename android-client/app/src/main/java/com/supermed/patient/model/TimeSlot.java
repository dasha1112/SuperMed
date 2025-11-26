// app/src/main/java/com/supermed/patient/model/TimeSlot.java
package com.supermed.patient.model;

public class TimeSlot {
    public String date;
    public String time;

    public TimeSlot() {}

    public TimeSlot(String date, String time) {
        this.date = date;
        this.time = time;
    }

    @Override
    public String toString() {
        return date + " " + time;
    }
}