package com.ericsson.graduates.team1.jenkinsinfo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JenkinsDataModelTests {

    private JenkinsDataModel jenkinsDataModel;

    @BeforeEach
    void before() {
        jenkinsDataModel = new JenkinsDataModel("test", 0, "DAY",0, 0, 0, 0);
    }

    @Test
    void testGettersAndSetters() {
        Assertions.assertEquals( 0, jenkinsDataModel.getId());
        jenkinsDataModel.setJobType("test1");
        Assertions.assertEquals("test1", jenkinsDataModel.getJobType());
        jenkinsDataModel.setNumberOfDeliveries(1);
        Assertions.assertEquals(1, jenkinsDataModel.getNumberOfDeliveries());
        jenkinsDataModel.setDurationTime(1);
        Assertions.assertEquals(1, jenkinsDataModel.getDurationTime());
        jenkinsDataModel.setSuccessRate(1);
        Assertions.assertEquals(1, jenkinsDataModel.getSuccessRate());
        jenkinsDataModel.setRestoreTime(1);
        Assertions.assertEquals(1, jenkinsDataModel.getRestoreTime());
    }

    @Test
    void testConstructor() {
        Assertions.assertEquals("test", jenkinsDataModel.getJobType());
        Assertions.assertEquals(0, jenkinsDataModel.getNumberOfDeliveries());
        Assertions.assertEquals(0, jenkinsDataModel.getDurationTime());
        Assertions.assertEquals(0, jenkinsDataModel.getSuccessRate());
        Assertions.assertEquals(0, jenkinsDataModel.getRestoreTime());
    }
}
