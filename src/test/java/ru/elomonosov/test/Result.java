package ru.elomonosov.test;

public class Result {

    private String text;

    private long absoluteTime;

    private double relativeTime;

    public Result(String text, long absoluteTime) {
        this.text = text;
        this.absoluteTime = absoluteTime;
    }

    public long getAbsoluteTime() {
        return absoluteTime;
    }

    public double getRelativeTime() {
        return relativeTime;
    }

    public void setRelativeTime(double relativeTime) {
        this.relativeTime = relativeTime;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}