package com.ericsson.graduates.team1.jenkinsinfo;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@CrossOrigin
public class JenkinsController {

    @Autowired
    private JenkinsDataModelDAO jenkinsDataModelDAO;

    private JenkinsServer jenkins;
    private JenkinsDataServiceImpl jenkinsDataService = new JenkinsDataServiceImpl();
    private Set<String> jobTitles;

    Logger myLogger = Logger.getLogger(JenkinsController.class.getName());

    public JenkinsController() {
        try {
            jenkins = new JenkinsServer(new URI("https://fem1s11-eiffel216.eiffel.gic.ericsson.se:8443/jenkins/"), "emdansi", "113715a4a81314ba2075d3fbddf3ca3059");
        } catch(URISyntaxException e) {
            myLogger.log(Level.ALL, "URI Syntax error");
        }
    }

    @GetMapping(value = "")
    public String index(Model model) {
        model.addAttribute("response", new JenkinsResponseObject());
        try {
            jobTitles =  jenkins.getJobs().keySet();
            model.addAttribute("jobs",jobTitles);
        } catch (IOException e) {
            return "error";
        }
        model.addAttribute("lastRun",ScheduleTasks.lastRun.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString());
        return "index";
    }

    @PostMapping(value = "/getRepository")
    public String getRepository(@ModelAttribute("response") JenkinsResponseObject response, Model model) {

        List<JenkinsDataModel> totalJobs = new ArrayList<JenkinsDataModel>();
        String period = response.getPeriod();
        List<JenkinsDataModel> savedJobs = new ArrayList<>();
        for (String jobname: response.getRepositoryName()) {
            savedJobs = (List<JenkinsDataModel>) jenkinsDataModelDAO.findAllByJobTypeAndTimeScale(jobname, response.getPeriod());
            totalJobs.addAll(savedJobs);
        }

        if(totalJobs.isEmpty()){
            model.addAttribute("errorMsg","The Jenkins Job:" + Arrays.toString(response.getRepositoryName()) + " is still being cached. Please try again later");
        }
        model.addAttribute("dataModels", totalJobs);
        model.addAttribute("jobs",jobTitles);
        model.addAttribute("lastRun",ScheduleTasks.lastRun.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString());

        return "index";


    }

}
