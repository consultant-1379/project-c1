package com.ericsson.graduates.team1.jenkinsinfo;

import javax.persistence.*;

//Data to be sent to the front end, calculated as per specifications
@Entity
@Table(name="jobs")
public class JenkinsDataModel {
    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, name="job_type")
    private String jobType;
    @Column(nullable = false, name="last_build_number")
    private int lastBuildNumber;
    @Column(nullable = false, name = "time_scale")
    private String timeScale;
    @Column(nullable = false, name="number_of_deliveries")
    private int numberOfDeliveries;
    @Column(nullable = false, name="duration_time")
    private double durationTime;
    @Column(nullable = false, name="success_rate")
    private double successRate;
    @Column(nullable = false, name="restore_time")
    private double restoreTime;

    public JenkinsDataModel() {}

    @Override
    public String toString() {
        return "JenkinsDataModel{" +
                "id=" + id +
                ", jobType='" + jobType + '\'' +
                ", lastBuildNumber=" + lastBuildNumber +
                ", timeScale='" + timeScale + '\'' +
                ", numberOfDeliveries=" + numberOfDeliveries +
                ", durationTime=" + durationTime +
                ", successRate=" + successRate +
                ", restoreTime=" + restoreTime +
                '}';
    }

    public JenkinsDataModel(String jobType, int lastBuildNumber, String timeScale, int numberOfDeliveries, double durationTime, double successRate, double restoreTime) {
        this.jobType = jobType;
        this.lastBuildNumber = lastBuildNumber;
        this.timeScale = timeScale;
        this.numberOfDeliveries = numberOfDeliveries;
        this.durationTime = durationTime;
        this.successRate = successRate;
        this.restoreTime = restoreTime;
    }

    public int getId() {
        return id;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public int getNumberOfDeliveries() {
        return numberOfDeliveries;
    }

    public void setNumberOfDeliveries(int numberOfDeliveries) {
        this.numberOfDeliveries = numberOfDeliveries;
    }

    public double getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(double durationTime) {
        this.durationTime = durationTime;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public double getRestoreTime() {
        return restoreTime;
    }

    public int getLastBuildNumber() {
        return lastBuildNumber;
    }

    public void setLastBuildNumber(int lastBuildNumber) {
        this.lastBuildNumber = lastBuildNumber;
    }

    public String getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(String timeScale) {
        this.timeScale = timeScale;
    }

    public void setRestoreTime(double restoreTime) {
        this.restoreTime = restoreTime;
    }
}
