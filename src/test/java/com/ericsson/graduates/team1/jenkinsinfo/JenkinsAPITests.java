package com.ericsson.graduates.team1.jenkinsinfo;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
class JenkinsAPITests {

    private JenkinsServer jenkins;
    private String jenkinsJobOneName;
    private String jenkinsJobTwoName;
    private String jenkinsDisabledJobName;
    private static List<Date> dates;
    private static List<Date> datesForJobOne;
    private static List<Date> datesForJobTwo;

    @BeforeEach()
    private void setup() throws URISyntaxException {
        jenkins = new JenkinsServer(new URI("https://fem1s11-eiffel216.eiffel.gic.ericsson.se:8443/jenkins/"), "emdansi", "113715a4a81314ba2075d3fbddf3ca3059");
        jenkinsJobOneName = "adp-ref-catfacts-text-encoder-base64_PreCodeReview";
        // Chosen because it has only 33 builds at time of choice
        jenkinsJobTwoName = "adp-ref-catfacts-text-encoder-base64_Publish";
        // Chosen to use for when a build being added would affect test results
        jenkinsDisabledJobName = "idun-sdk_PreCodeReview_CIP-39731";
    }

    @Test
    void testGetJobsIsNotNull() throws IOException {
        assertThat(jenkins.getJobs()).isNotNull();
    }

    @Test
    void testGetJobWithDetailsIsNotNull() throws IOException {
        Map<String, Job> jobs = jenkins.getJobs();
        assertThat(jobs.get(jenkinsJobOneName).details()).isNotNull();
    }

    @Test
    void testGetBuildDurationForAllBuildsIsNotNull() throws IOException {
        Map<String, Job> jobs = jenkins.getJobs();
        JobWithDetails job = jobs.get(jenkinsJobOneName).details();
        List<Build> builds = job.getAllBuilds();
        boolean listNotNull = true;
        for (Build b : builds) {
            if ((Long) b.details().getDuration() == null) {
                listNotNull = false;
                break;
            }
        }

        assertThat(listNotNull).isTrue();
    }

    // ********************************
    //           OLD TESTS
    // ********************************

    // Test what getNumberOfJobsRunForDay returns against hard coded values for
    // what is known for particular days for the jenkins job
    @ParameterizedTest(name = " date: {1} => should return {0}")
    @MethodSource("jenkinsAPINumJobsTestData")
    void testCalculateNumberOfDeliveriesForDay(int expectedNoDays, Date date) throws IOException {
        Job job = jenkins.getJob(jenkinsJobOneName);
        System.out.println("JOB NAME : " + job.getName());
        JenkinsDataService service = new JenkinsDataServiceImpl();

        assertThat(expectedNoDays, is(service.calculateNumberOfDeliveriesForDay(job, date)));
    }

    @ParameterizedTest(name = " date: {1} => should return {0}")
    @MethodSource("jenkinsAPIPassRateTestData")
    void testCalculateSuccessRate(double expectedRate, Date date) throws IOException {
        Map<String, Job> jobs = jenkins.getJobs();
        Job job = jobs.get(jenkinsJobOneName);
        JenkinsDataService service = new JenkinsDataServiceImpl();
        DecimalFormat frmt = new DecimalFormat();
        frmt.setMaximumFractionDigits(2);

        assertThat(frmt.format(expectedRate), is(frmt.format(service.calculateSuccessRate(job, date))));
    }

    // ********************************
    //           NEW TESTS
    // ********************************

    @ParameterizedTest(name = " date: {1} and period: {2} => should return {0}")
    @MethodSource("jenkinsAPINumJobsForPeriodTestData")
    public void testCalculateNumberOfDeliveriesForPeriod(int expectedNoDeliveries, Date date, Period period) throws IOException, URISyntaxException {
        Job jobOne = jenkins.getJob(jenkinsJobTwoName);
        Job jobTwo = jenkins.getJob(jenkinsDisabledJobName);
        JenkinsDataService serviceOne = new JenkinsDataServiceImpl();
        JenkinsDataService serviceTwo = new JenkinsDataServiceImpl();
        if (period != Period.YEAR)
            assertThat(expectedNoDeliveries, is(serviceOne.calculateNumberOfDeliveriesForPeriod(jobOne, date, period)));
        else
            assertThat(expectedNoDeliveries, is(serviceTwo.calculateNumberOfDeliveriesForPeriod(jobTwo, date, period)));
    }

    @Test
    public void testCalculateAverageDurationTime() throws IOException, URISyntaxException {
        Job job = jenkins.getJob(jenkinsDisabledJobName);
        JenkinsDataService service = new JenkinsDataServiceImpl();
        DecimalFormat frmt = new DecimalFormat();
        frmt.setMaximumFractionDigits(2);

        assertThat(frmt.format(45269.2), is(frmt.format(service.calculateAverageDurationTime(job))));
    }

    @ParameterizedTest(name = " date: {1} for period {2} => should return {0}")
    @MethodSource("jenkinsAPIPassRateForPeriodTestData")
    public void testCalculateSuccessRateForPeriod(double expectedRate, Date date, Period period) throws IOException, URISyntaxException {
        Job jobOne = jenkins.getJob(jenkinsJobTwoName);
        Job jobTwo = jenkins.getJob(jenkinsDisabledJobName);
        JenkinsDataService serviceOne = new JenkinsDataServiceImpl();
        JenkinsDataService serviceTwo = new JenkinsDataServiceImpl();
        DecimalFormat frmt = new DecimalFormat();
        frmt.setMaximumFractionDigits(2);

        if (period != Period.YEAR)
            assertThat(frmt.format(expectedRate), is(frmt.format(serviceOne.calculateSuccessRateForPeriod(jobOne, date, period))));
        else
            assertThat(frmt.format(expectedRate), is(frmt.format(serviceTwo.calculateSuccessRateForPeriod(jobTwo, date, period))));
    }

    @Test
    public void testCalculateRestoreTimeForBuild() throws IOException, URISyntaxException {
        Job job = jenkins.getJob(jenkinsJobTwoName);
        JenkinsDataService service = new JenkinsDataServiceImpl();
        List<BuildWithDetails> builds = new ArrayList<>();
        builds.add(job.details().getBuildByNumber(15).details());
        builds.add(job.details().getBuildByNumber(10).details());
        builds.add(job.details().getBuildByNumber(4).details());

        assertThat(2686L, is(service.calculateRestoreTimeForBuild(job, builds.get(0))));
        assertThat(563L, is(service.calculateRestoreTimeForBuild(job, builds.get(1))));
        assertThat(105080L, is(service.calculateRestoreTimeForBuild(job, builds.get(2))));
    }

    @Test
    public void testCalculateAverageRestoreTime() throws URISyntaxException, IOException {
        Job job = jenkins.getJob(jenkinsJobTwoName);
        JenkinsDataService service = new JenkinsDataServiceImpl();
        DecimalFormat frmt = new DecimalFormat();
        frmt.setMaximumFractionDigits(0);

        assertThat(frmt.format(118980.18), is(frmt.format(service.calculateAverageRestoreTime(job))));
    }

    // TO BE REPLACED WITH NEW TEST DATA
    static {
        dates = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(2021, Calendar.AUGUST, 5);
        dates.add(cal.getTime());
        cal.set(2021, Calendar.JULY, 29);
        dates.add(cal.getTime());
        cal.set(2021, Calendar.JULY, 28);
        dates.add(cal.getTime());
        cal.set(2021, Calendar.JULY, 7);
        dates.add(cal.getTime());
    }

    private static Stream<Arguments> jenkinsAPINumJobsTestData() {
        return Stream.of(
                Arguments.of(2, dates.get(0)),
                Arguments.of(1, dates.get(1)),
                Arguments.of(7, dates.get(2))
        );
    }

    private static Stream<Arguments> jenkinsAPIPassRateTestData() {
        return Stream.of(
                Arguments.of(0, dates.get(0)),
                Arguments.of(0, dates.get(1)),
                Arguments.of(0, dates.get(2)),
                Arguments.of(0, dates.get(3))
        );
    }

    // NEW METHODS TEST DATA
    static {
        datesForJobOne = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(2021, Calendar.AUGUST, 5);
        datesForJobOne.add(cal.getTime());

        cal.set(2021, Calendar.MAY, 10);
        datesForJobOne.add(cal.getTime());

        datesForJobTwo = new ArrayList<>();
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2021, Calendar.JULY, 20);
        datesForJobTwo.add(cal2.getTime());
    }

    private static Stream<Arguments> jenkinsAPINumJobsForPeriodTestData() {
        return Stream.of(
                Arguments.of(1, datesForJobOne.get(0), Period.DAY), // DAY
                Arguments.of(3, datesForJobOne.get(0), Period.WEEK), // WEEK
                Arguments.of(4, datesForJobOne.get(0), Period.MONTH),  // MONTH
                Arguments.of(15, datesForJobTwo.get(0), Period.YEAR)  // YEAR
        );
    }

    private static Stream<Arguments> jenkinsAPIPassRateForPeriodTestData() {
        return Stream.of(
                Arguments.of(0.5, datesForJobOne.get(1), Period.DAY),
                Arguments.of(0.5, datesForJobOne.get(1), Period.WEEK),
                Arguments.of(0.47, datesForJobOne.get(1), Period.MONTH),
                Arguments.of(0.27, datesForJobTwo.get(0), Period.YEAR)
        );
    }


}
