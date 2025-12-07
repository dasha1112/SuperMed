package com.supermed.patient.ui;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.assertNull;

import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@RunWith(AndroidJUnit4.class)
public class BranchActivityTest {

    private MockWebServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        overrideApiClientBaseUrl(server.url("/"));
    }

    @After
    public void tearDown() throws Exception {
        overrideApiClientBaseUrl(null); // reset retrofit back to null
        if (server != null) {
            server.shutdown();
        }
    }

    @Test
    public void branchesAreRenderedFromServer() throws Exception {
        Buffer body = new Buffer();
        body.writeUtf8("[{\"id\":1,\"name\":\"Clinic A\",\"address\":\"Address 1\"},{\"id\":2,\"name\":\"Clinic B\",\"address\":\"Address 2\"}]");
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        try (ActivityScenario<BranchActivity> scenario = ActivityScenario.launch(BranchActivity.class)) {
            onView(isRoot()).perform(waitFor(TimeUnit.SECONDS.toMillis(1)));
            onData(anything())
                    .inAdapterView(withId(R.id.list_view))
                    .atPosition(0)
                    .onChildView(withId(R.id.tv_branch_name))
                    .check(matches(withText("Clinic A")));
        }
    }

    private void overrideApiClientBaseUrl(Object url) throws Exception {
        Field retrofitField = ApiClient.class.getDeclaredField("retrofit");
        retrofitField.setAccessible(true);
        if (url == null) {
            retrofitField.set(null, null);
            return;
        }
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(String.valueOf(url))
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
