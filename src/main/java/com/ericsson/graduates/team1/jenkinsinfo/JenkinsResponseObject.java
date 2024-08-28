package com.ericsson.graduates.team1.jenkinsinfo;

public class JenkinsResponseObject {
    private String [] repositoryName;

    public String[] getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String[] repositoryName) {
        this.repositoryName = repositoryName;
    }

    private String period;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

}
