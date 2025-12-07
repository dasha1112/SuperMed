package com.supermed.patient.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.supermed.patient.R;
import com.supermed.patient.api.ApiClient;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@RunWith(AndroidJUnit4.class)
public class RegisterActivityTest {

    private MockWebServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
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
    public void mismatchedPasswords_doesNotFinish() {
        try (ActivityScenario<RegisterActivity> scenario = ActivityScenario.launch(RegisterActivity.class)) {
            onView(withId(R.id.et_reg_username)).perform(replaceText("user"), closeSoftKeyboard());
            onView(withId(R.id.et_reg_password)).perform(replaceText("pass1"), closeSoftKeyboard());
            onView(withId(R.id.et_reg_password_confirm)).perform(replaceText("pass2"), closeSoftKeyboard());
            onView(withId(R.id.btn_register_submit)).perform(click());

            scenario.onActivity(activity -> assertFalse(activity.isFinishing()));
        }
    }

    @Test
    public void successfulRegistration_finishesActivity() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"success\":true,\"message\":\"ok\",\"user\":{\"username\":\"new.user\"}}"));

        try (ActivityScenario<RegisterActivity> scenario = ActivityScenario.launch(RegisterActivity.class)) {
            onView(withId(R.id.et_reg_username)).perform(replaceText("new.user"), closeSoftKeyboard());
            onView(withId(R.id.et_reg_password)).perform(replaceText("pw123"), closeSoftKeyboard());
            onView(withId(R.id.et_reg_password_confirm)).perform(replaceText("pw123"), closeSoftKeyboard());
            onView(withId(R.id.btn_register_submit)).perform(click());

            scenario.onActivity(activity -> assertTrue(activity.isFinishing()));
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
}
