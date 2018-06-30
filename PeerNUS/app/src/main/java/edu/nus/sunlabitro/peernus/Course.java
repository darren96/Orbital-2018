package edu.nus.sunlabitro.peernus;

public class Course {
    private int id;
    private String courseName;
    private String faculty;

    public Course(int id, String courseName, String faculty) {
        this.id = id;
        this.courseName = courseName;
        this.faculty = faculty;
    }

    public int getID() {
        return this.id;
    }

    public String getCourseName() {
        return this.courseName;
    }

    public String getFaculty() {
        return this.faculty;
    }

}
