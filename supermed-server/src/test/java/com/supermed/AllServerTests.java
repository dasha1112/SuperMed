package com.supermed;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("All server tests")
@SelectClasses({
        ModelDoctorTest.class,
        ModelAppointmentTest.class,
        HashTest.class,
        ModelScheduleTest.class,
        ModelBranchTest.class,
        ModelMessageTest.class,
        ModelStatisticsTest.class
})
public class AllServerTests { }