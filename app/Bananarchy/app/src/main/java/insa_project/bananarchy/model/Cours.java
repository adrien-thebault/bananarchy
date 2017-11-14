package insa_project.bananarchy.model;

/**
 * Created by pierre on 13/11/17.
 */

public class Cours {

    private long beginning;
    private long end;
    private int duration;
    private String summary;
    private String location;
    private String teacher;

    public Cours(long beginning, int duration, long end, String summary, String location, String teacher) {
        this.beginning = beginning;
        this.duration = duration;
        this.end = end;
        this.summary = summary;
        this.location = location;
        this.teacher = teacher;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getBeginning() {
        return beginning;
    }

    public void setBeginning(long beginning) {
        this.beginning = beginning;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

}
