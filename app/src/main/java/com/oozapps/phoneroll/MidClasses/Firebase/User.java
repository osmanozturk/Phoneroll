package com.oozapps.phoneroll.MidClasses.Firebase;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for Firebase User
 */
public class User {
    String name;
    List<Course> courseList;
    List<List<Lecture>> week;

    public User() {
        name = "EMPTY";
        courseList = new ArrayList<>();
        week = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            week.add(new ArrayList<Lecture>());
        }
    }

    public User(String name, List<Course> courseList, List<List<Lecture>> week) {
        this.name = name;
        this.week = week;
        this.courseList = courseList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Course> getCourseList() {
        return courseList;
    }

    public void setCourseList(List<Course> courseList) {
        this.courseList = courseList;
    }

    public List<List<Lecture>> getWeek() {
        return week;
    }

    public void setWeek(List<List<Lecture>> week) {
        this.week = week;
    }

}
