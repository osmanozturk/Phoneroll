package com.oozapps.phoneroll.MidClasses.Firebase;

import java.util.Date;

public class Course {
    private String courseCode;
    private int totalCount;
    private int attendCount;
    private Date lastAttendDate;

    public Course() {
        courseCode = "EMPTY";
        totalCount = 0;
        attendCount = 0;
        lastAttendDate = new Date();
    }

    public Course(String courseCode, int totalCount, int attendCount, Date lastAttendDate) {
        this.courseCode = courseCode;
        this.totalCount = totalCount;
        this.attendCount = attendCount;
        this.lastAttendDate = lastAttendDate;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getAttendCount() {
        return attendCount;
    }

    public void setAttendCount(int attendCount) {
        this.attendCount = attendCount;
    }

    public Date getLastAttendDate() {
        return lastAttendDate;
    }

    public void setLastAttendDate(Date lastAttendDate) {
        this.lastAttendDate = lastAttendDate;
    }
}
