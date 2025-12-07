package com.supermed.patient.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ListView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.supermed.patient.R;
import com.supermed.patient.api.ApiClient;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@RunWith(AndroidJUnit4.class)
public class MyAppointmentsActivityTest {

    private MockWebServer server;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        prefs.edit().clear().putString("username", "p.kotova").commit();

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
    //экран «Мои записи» должен показывать только записи текущего пользователя, даже если сервер отдаёт всех.
    @Test
    public void showsOnlyCurrentUserAppointments() {
        server.enqueue(new MockResponse().setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("[" +
                        "{\"id\":1,\"patientUsername\":\"p.kotova\",\"doctorId\":2,\"appointmentDate\":\"2025-02-10\",\"startTime\":\"10:00\",\"endTime\":\"10:30\",\"secretId\":\"SEC123\",\"status\":\"scheduled\",\"doctorName\":\"Dr. Ivanov\"}," +
                        "{\"id\":2,\"patientUsername\":\"other\",\"doctorId\":3,\"appointmentDate\":\"2025-02-11\",\"startTime\":\"11:00\",\"endTime\":\"11:30\",\"secretId\":\"SEC999\",\"status\":\"scheduled\",\"doctorName\":\"Dr. Smith\"}" +
                        "]"));

        try (ActivityScenario<MyAppointmentsActivity> scenario = ActivityScenario.launch(MyAppointmentsActivity.class)) {
            onView(isRoot()).perform(waitFor(TimeUnit.SECONDS.toMillis(1)));
            scenario.onActivity(activity -> {
                ListView listView = activity.findViewById(R.id.list_view);
                assertEquals(1, listView.getAdapter().getCount());
            });
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
                StringDescription description = new StringDescription();
                description.appendText("wait for " + delayMs + " milliseconds.");
                return description.toString();
            }

            @Override
            public void perform(androidx.test.espresso.UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(delayMs);
            }
        };
    }
}
