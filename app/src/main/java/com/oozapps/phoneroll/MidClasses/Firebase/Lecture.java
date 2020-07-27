package com.oozapps.phoneroll.MidClasses.Firebase;

/**
 * Lecture That will be held in a day
 */
public class Lecture {
    private TimeOfDay start;
    private TimeOfDay end;
    private String courseCode;
    private String roomName;

    public Lecture() {
        start = new TimeOfDay();
        end = new TimeOfDay();
        courseCode = "EMPTY";
        roomName = "EMPTY";
    }

    public Lecture(TimeOfDay start, TimeOfDay end, String courseCode, String roomName) {
        this.start = start;
        this.end = end;
        this.courseCode = courseCode;
        this.roomName = roomName;
    }

    public TimeOfDay getStart() {
        return start;
    }

    public void setStart(TimeOfDay start) {
        this.start = start;
    }

    public TimeOfDay getEnd() {
        return end;
    }

    public void setEnd(TimeOfDay end) {
        this.end = end;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
