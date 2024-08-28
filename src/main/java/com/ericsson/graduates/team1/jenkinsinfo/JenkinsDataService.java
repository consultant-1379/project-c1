package com.ericsson.graduates.team1.jenkinsinfo;

import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.Job;

import java.io.IOException;
import java.util.Date;

public interface JenkinsDataService {

    // TO BE DEPRECATED
    public int calculateNumberOfDeliveriesForDay(Job job, Date date) throws IOException;
    public double calculateDurationTime(Job job) throws IOException;
    public double calculateSuccessRate(Job job, Date date) throws IOException;
    public double calculateRestoreTime(Job job) throws IOException;

    // NEW METHODS
    public int calculateNumberOfDeliveriesForPeriod(Job job, Date date, Period period) throws IOException;
    public double calculateAverageDurationTime(Job job) throws IOException;
    public double calculateSuccessRateForPeriod(Job job, Date date, Period period) throws IOException;
    public long calculateRestoreTimeForBuild(Job job, BuildWithDetails build) throws IOException;
    public double calculateAverageRestoreTime(Job job) throws IOException;

}
