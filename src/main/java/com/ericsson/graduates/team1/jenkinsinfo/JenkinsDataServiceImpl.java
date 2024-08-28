package com.ericsson.graduates.team1.jenkinsinfo;

import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.Job;

import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

//This class will be used to calculate each of these values before storing it into a JenkinsDataModel object to send to the frontend
public class JenkinsDataServiceImpl implements JenkinsDataService {

    private List<BuildWithDetails> buildsWithDetails = new LinkedList<>();

    // To be replaced by period counter-part
    @Override
    public int calculateNumberOfDeliveriesForDay(Job job, Date date) throws IOException {
        if (buildsWithDetails.isEmpty())
            cacheBuildsWithDetails(job);

        List<BuildWithDetails> buildsOnDay = getBuildsForDay(date);

        return buildsOnDay.size();
    }

    // To be replaced by period counter-part
    @Override
    public double calculateSuccessRate(Job job, Date date) throws IOException {
        List<BuildWithDetails> buildsOnDay = getBuildsForDay(date);
        int buildsTotalCount = buildsOnDay.size();
        long buildsPassCount = getNumPassedBuilds(buildsOnDay);

        return calculateDecimalPlace((double)buildsPassCount / (double)buildsTotalCount * 100.0);
    }

    // To be replaced by build-specified counter-part
    @Override
    public double calculateRestoreTime(Job job) throws IOException {
        Build lastFailedBuild = job.details().getLastFailedBuild();
        if(lastFailedBuild.getNumber() == job.details().getLastBuild().getNumber()){
            return 0;
        }

        LocalDateTime failedBuildStamp = new Timestamp(lastFailedBuild.details().getTimestamp()).toLocalDateTime();
        int failedBuildIndex = lastFailedBuild.getNumber();
        Build nextSuccessfulBuild = lastFailedBuild;
        int currBuildIndex = failedBuildIndex+1;
        while (nextSuccessfulBuild.details().getResult().name().equals("FAILURE")) {
            nextSuccessfulBuild = job.details().getBuildByNumber(currBuildIndex);
            currBuildIndex++;
        }

        buildsWithDetails.clear();
        LocalDateTime successBuildStamp = new Timestamp(nextSuccessfulBuild.details().getTimestamp()).toLocalDateTime();
        return (Duration.between(failedBuildStamp,successBuildStamp).toMillis() / 1000.0);
    }

    @Override
    public int calculateNumberOfDeliveriesForPeriod(Job job, Date date, Period period) throws IOException {
        if (buildsWithDetails.isEmpty())
            cacheBuildsWithDetails(job);

        List<BuildWithDetails> buildsForPeriod = getBuildsForPeriod(date, period);

        return buildsForPeriod.size();
    }

    @Override
    public double calculateDurationTime(Job job) throws IOException {
        return calculateDecimalPlace(job.details().getLastBuild().details().getDuration() * 0.001);
    }

    @Override
    public double calculateAverageDurationTime(Job job) throws IOException {
        if (buildsWithDetails.isEmpty())
            cacheBuildsWithDetails(job);

        double totalDuration = 0.0;
        for (BuildWithDetails b : buildsWithDetails)
            totalDuration += b.getDuration();

        return totalDuration / (double)(buildsWithDetails.size());
    }

    @Override
    public double calculateSuccessRateForPeriod(Job job, Date date, Period period) throws IOException {
        if (buildsWithDetails.isEmpty())
            cacheBuildsWithDetails(job);

        List<BuildWithDetails> buildsForPeriod = getBuildsForPeriod(date, period);
        if(buildsForPeriod.isEmpty()){
            return 0;
        }
        int buildsTotalCount = buildsForPeriod.size();
        long buildsPassCount = getNumPassedBuilds(buildsForPeriod);

        return calculateDecimalPlace((double)buildsPassCount / (double)buildsTotalCount * 100.0);
    }

    @Override
    public long calculateRestoreTimeForBuild(Job job, BuildWithDetails build) throws IOException {
        if (buildsWithDetails.isEmpty())
            cacheBuildsWithDetails(job);

        if (!build.getResult().name().equals("FAILURE")) {
            System.out.println("Provided build was successful. Defaulting to last failed build...");
            build = job.details().getLastFailedBuild().details();
        }

        LocalDateTime failedBuildTime =
                new Timestamp(build.getTimestamp()).toLocalDateTime();
        int failedBuildIndex = build.getNumber();
        BuildWithDetails nextSuccessfulBuild = build;

        int currBuildIndex = failedBuildIndex+1;
        while (nextSuccessfulBuild.getResult().name().equals("FAILURE")) {
            nextSuccessfulBuild = job.details().getBuildByNumber(currBuildIndex).details();
            currBuildIndex++;
            if (currBuildIndex > buildsWithDetails.size()) {
                return 0;
            }
        }
        LocalDateTime successfulBuildTime =
                new Timestamp(nextSuccessfulBuild.getTimestamp()).toLocalDateTime();

        double y = ((Duration.between(failedBuildTime, successfulBuildTime)).toMillis() / 1000.0);
        long restoreTime = (long) Math.floor( ((Duration.between(failedBuildTime, successfulBuildTime)).toMillis() / 1000.0) );

        buildsWithDetails.clear();

        return restoreTime;
    }

    @Override
    public double calculateAverageRestoreTime(Job job) throws IOException {
        if (buildsWithDetails.isEmpty())
            cacheBuildsWithDetails(job);

        List<BuildWithDetails> failedBuilds =
                buildsWithDetails.stream().filter(b -> b.getResult().name().equals("FAILURE")).collect(Collectors.toList());
        double totalRestoreTime = 0.0;
        for (BuildWithDetails b : failedBuilds)
            totalRestoreTime += calculateRestoreTimeForBuild(job, b);

        return totalRestoreTime / failedBuilds.size();
    }

    // HELPER METHODS

    private List<BuildWithDetails> getBuildsForDay(Date date) {
        LocalDate localDateProvided = new Timestamp(date.getTime()).toLocalDateTime().toLocalDate();
        List<BuildWithDetails> buildsOnDay = new ArrayList<>();
        for (BuildWithDetails b : buildsWithDetails) {
            LocalDate localDateBuild = new Timestamp(new Date(b.getTimestamp()).getTime()).toLocalDateTime().toLocalDate();
            boolean sameDay = checkDatesAreSameDay(localDateProvided, localDateBuild);
            if (sameDay) {
                buildsOnDay.add(b);
            }
        }

        return buildsOnDay;
    }

    private boolean checkDatesAreSameDay(LocalDate localDateOne, LocalDate localDateTwo) {
        if (localDateOne.getYear() == localDateTwo.getYear() &&
            localDateOne.getMonth() == localDateTwo.getMonth() &&
            localDateOne.getDayOfMonth() == localDateTwo.getDayOfMonth()) {
            return true;
        }
        return false;
    }

    private List<BuildWithDetails> getBuildsForPeriod(Date date, Period period) throws IOException {
        LocalDate localDateProvided = new Timestamp(date.getTime()).toLocalDateTime().toLocalDate();
        List<BuildWithDetails> buildsForPeriod = new ArrayList<>();

        if (period.name().equals("ALL"))
            return buildsWithDetails;

        for (BuildWithDetails b : buildsWithDetails) {
            LocalDate buildDate = new Timestamp(
                    new Date(b.getTimestamp()).getTime()).toLocalDateTime().toLocalDate();
            boolean samePeriod = checkDatesAreSamePeriod(localDateProvided, buildDate, period);

            if (samePeriod)
                buildsForPeriod.add(b);
        }

        return buildsForPeriod;
    }

    private boolean checkDatesAreSamePeriod(LocalDate dateOne, LocalDate dateTwo, Period period) {
        Integer weekOne = getWeekOfYearFromLocalDate(dateOne);
        Integer weekTwo = getWeekOfYearFromLocalDate(dateTwo);

        if (dateOne.getYear() == dateTwo.getYear()) {
            if (period.name().equals("YEAR")) {
                return true;
            } else if (dateOne.getMonthValue() == dateTwo.getMonthValue()) {
                if (period.name().equals("MONTH")) {
                    return true;
                } else if (weekOne == weekTwo) {
                    if (period.name().equals("WEEK")) {
                        return true;
                    } else if (dateOne.getDayOfMonth() == dateTwo.getDayOfMonth()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private Integer getWeekOfYearFromLocalDate(LocalDate localDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(convertLocalDateToDate(localDate));
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    private Date convertLocalDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    private long getNumPassedBuilds(List<BuildWithDetails> builds) {
        return builds.stream().filter(b -> b.getResult().name().equals("SUCCESS")).count();
    }

    private double calculateDecimalPlace(double number){
        double scale = Math.pow(10, 2);
        return Math.round(number * scale) / scale;
    }

    private void cacheBuildsWithDetails(Job job) throws IOException {
        for(Build build : job.details().getAllBuilds()) {
            buildsWithDetails.add(build.details());
        }
    }

}
