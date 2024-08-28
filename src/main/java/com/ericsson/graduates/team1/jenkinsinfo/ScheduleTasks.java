package com.ericsson.graduates.team1.jenkinsinfo;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Configuration
@EnableScheduling
public class ScheduleTasks {
    Logger myLogger = Logger.getLogger(JenkinsController.class.getName());
    public static LocalDateTime lastRun;
    private JenkinsServer jenkins;
    private JenkinsDataServiceImpl jenkinsDataService = new JenkinsDataServiceImpl();

    @Autowired
    private JenkinsDataModelDAO jenkinsDataModelDAO;

    @Scheduled(initialDelay = 1000, fixedDelay = 1200000)
    public void cacheJenkinsData(){
        lastRun = LocalDateTime.now();
        try {
            jenkins = new JenkinsServer(new URI("https://fem1s11-eiffel216.eiffel.gic.ericsson.se:8443/jenkins/"), "emdansi", "113715a4a81314ba2075d3fbddf3ca3059");
            Map<String, Job> listOfJobs = jenkins.getJobs();
            for(String jobname : listOfJobs.keySet()){
                for(Period timeScale : Period.values()) {
                    processCache(listOfJobs, jobname, timeScale);
                }
            }
        } catch (URISyntaxException | IOException e) {
            myLogger.log(Level.ALL, "URI Syntax error");
        }
    }

    private void processCache(Map<String, Job> listOfJobs, String jobname, Period timeScale) throws IOException {
        System.out.println("\n\nCACHING:" + jobname + "\tfor TIME PERIOD:\t" + timeScale);
        Job currentJob = listOfJobs.get(jobname);

        // If job is building, move on
        if (currentJob.details().getLastBuild().details().isBuilding()) {
            System.out.println("JOB IS CURRENTLY ACTIVE:\t" + jobname);
            return;
        }

        // If no builds before current year, assign cache for period 'ALL' the same values as for period 'YEAR'
        boolean firstBuildOnThisYear = checkFirstBuildIsThisYear(currentJob);
        if (timeScale.name().equals("ALL") && firstBuildOnThisYear) {
            setALLCacheValuesToYEARCacheValues(jobname);
            return;
        }

        // Decide whether to save update or skip cache
        List<JenkinsDataModel> previousJobs = (List<JenkinsDataModel>) jenkinsDataModelDAO.findAllByJobTypeAndTimeScale(jobname, timeScale.name());
        saveUpdateOrSkipCache(jobname, previousJobs, currentJob, timeScale);
    }

    private void setALLCacheValuesToYEARCacheValues(String jobname) {
        System.out.println("NO BUILDS BEFORE CURRENT YEAR - ALL CACHE SAME AS YEAR CACHE");
        List<JenkinsDataModel> jobs = (List<JenkinsDataModel>) jenkinsDataModelDAO.findAllByJobTypeAndTimeScale(jobname, "YEAR");
        JenkinsDataModel dataModel = jobs.get(0);
        JenkinsDataModel dataModelCopy = new JenkinsDataModel(dataModel.getJobType(), dataModel.getLastBuildNumber(),
                "ALL", dataModel.getNumberOfDeliveries(), dataModel.getDurationTime(), dataModel.getSuccessRate(),
                dataModel.getRestoreTime());
        jenkinsDataModelDAO.save(dataModelCopy);
        System.out.println("CACHE COMPLETED FOR:\t" + jobname + "\tfor TIME PERIOD:\tALL");
    }

    private void saveUpdateOrSkipCache(String jobname, List<JenkinsDataModel> previousJobs, Job currentJob, Period timeScale) throws IOException {
        if(previousJobs.isEmpty())
            saveDataModelAsIs(jobname, currentJob, timeScale);
        else if(!previousJobs.isEmpty()){
            JenkinsDataModel previousJob = previousJobs.get(0);
            if(previousJob.getLastBuildNumber() < currentJob.details().getLastBuild().getNumber())
                saveUpdatedDataModel(jobname, previousJob, currentJob, timeScale);

        }
        System.out.println("CACHE COMPLETED FOR:\t" + jobname + "\tfor TIME PERIOD:\t" + timeScale);
    }

    private void saveDataModelAsIs(String jobname, Job currentJob, Period timeScale) throws IOException {
        System.out.println("SAVING:\t" + jobname + "\tTO DB\t" + LocalDateTime.now());
        jenkinsDataModelDAO.save(getDataModelForJob(currentJob, timeScale));
        System.out.println("SAVED: \t" + jobname + "\tAT :\t" + LocalDateTime.now());
    }

    private void saveUpdatedDataModel(String jobname, JenkinsDataModel previousJob, Job currentJob, Period timeScale) throws IOException {
        System.out.println("UPDATING:\t" + jobname +"\tTO DB\t" + LocalDateTime.now());
        jenkinsDataModelDAO.save(updateDataModel(previousJob, currentJob, timeScale));
        System.out.println("UPDATED:\t" + jobname + "\tAT :\t" + LocalDateTime.now());
    }

    private JenkinsDataModel getDataModelForJob(Job job, Period timeScale) throws IOException {
        Calendar myCal = Calendar.getInstance();
        Date myDate = myCal.getTime();
        String jobName = job.details().getDisplayName();
        int lastBuildNumber = job.details().getLastBuild().getNumber();
        int numOfDeliveries = jenkinsDataService.calculateNumberOfDeliveriesForPeriod(job,myDate,timeScale);
        double durationTime = jenkinsDataService.calculateDurationTime(job); // can be cached on first period (DAY) and persisted across
        double successRate = jenkinsDataService.calculateSuccessRateForPeriod(job,myDate,timeScale);
        double restoreTime = jenkinsDataService.calculateRestoreTime(job); // can be cached on first period (DAY) and persisted across
        return new JenkinsDataModel(jobName, lastBuildNumber, timeScale.name(), numOfDeliveries, durationTime, successRate, restoreTime);
    }

    private JenkinsDataModel updateDataModel(JenkinsDataModel previousJobData, Job currentJobData, Period timeScale) throws IOException {
        JenkinsDataModel updatedJobData = getDataModelForJob(currentJobData, timeScale);
        previousJobData.setLastBuildNumber(updatedJobData.getLastBuildNumber());
        previousJobData.setNumberOfDeliveries(updatedJobData.getNumberOfDeliveries());
        previousJobData.setDurationTime(updatedJobData.getDurationTime());
        previousJobData.setSuccessRate(updatedJobData.getSuccessRate());
        previousJobData.setRestoreTime(updatedJobData.getRestoreTime());
        return previousJobData;
    }

    private boolean checkFirstBuildIsThisYear(Job job) throws IOException {
        int currentYear = LocalDateTime.now().getYear();
        int buildYear = new Timestamp( new Date( job.details().getFirstBuild().details().getTimestamp() ).getTime() ).toLocalDateTime().getYear();
        return currentYear == buildYear;
    }


}
