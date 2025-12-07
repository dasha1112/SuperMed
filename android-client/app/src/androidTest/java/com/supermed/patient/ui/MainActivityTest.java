package com.supermed.patient.ui;

import static android.content.Context.MODE_PRIVATE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

import android.view.View;
import android.content.Context;
import android.content.SharedPreferences;
import android.app.Instrumentation;
import android.app.Activity;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.supermed.patient.R;
import com.supermed.patient.api.ApiClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import org.hamcrest.Matcher;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private Context context;
    private MockWebServer server;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences("user_session", MODE_PRIVATE);
        prefs.edit().clear().putString("username", "tester").commit();

        server = new MockWebServer();
        server.start();
        server.enqueue(new MockResponse().setResponseCode(200).setBody("[{\"id\":1,\"name\":\"Clinic A\",\"address\":\"Addr\"}]")); // branches
        server.enqueue(new MockResponse().setResponseCode(200).setBody("[]")); // appointments
        overrideApiClientBaseUrl(server.url("/").toString());
    }

    @After
    public void tearDown() throws Exception {
        overrideApiClientBaseUrl(null);
        if (server != null) {
            server.shutdown();
        }
    }

    @Test
    public void logoutClearsSession() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.btn_logout)).perform(click());

            SharedPreferences prefs = context.getSharedPreferences("user_session", MODE_PRIVATE);
            assertFalse(prefs.contains("username"));
        }
    }

    @Test
    public void navigationOpensBranchesAndAppointments() throws Exception {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor branchMonitor = instrumentation.addMonitor(BranchActivity.class.getName(), null, false);
        Instrumentation.ActivityMonitor appointmentsMonitor = instrumentation.addMonitor(MyAppointmentsActivity.class.getName(), null, false);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(isRoot()).perform(waitFor(500));
            onView(withId(R.id.btn_branches)).perform(click());
            Activity branch = instrumentation.waitForMonitorWithTimeout(branchMonitor, 2000);
            assertNotNull(branch);
            branch.finish();

            onView(isRoot()).perform(waitFor(500));
            onView(withId(R.id.btn_my_appointments)).perform(click());
            Activity appointments = instrumentation.waitForMonitorWithTimeout(appointmentsMonitor, 2000);
            assertNotNull(appointments);
            appointments.finish();
        }
    }

    private void overrideApiClientBaseUrl(String url) throws Exception {
        Field retrofitField = ApiClient.class.getDeclaredField("retrofit");
        retrofitField.setAccessible(true);
        if (url == null) {
            retrofitField.set(null, null);
            return;
        }
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        retrofitField.set(null, retrofit);
    }

    private static androidx.test.espresso.ViewAction waitFor(long delayMs) {
        return new androidx.test.espresso.ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for " + delayMs + " milliseconds.";
            }

            @Override
            public void perform(androidx.test.espresso.UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(delayMs);
            }
        };
    }
}
