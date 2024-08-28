package com.ericsson.graduates.team1.jenkinsinfo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface JenkinsDataModelDAO extends CrudRepository<JenkinsDataModel, Integer> {
    Iterable<JenkinsDataModel> findAllByJobType(String string);
    Iterable<JenkinsDataModel> findAllByJobTypeAndTimeScale(String jobType, String period);
}
