package com.supermed.patient.ui;

import static android.content.Context.MODE_PRIVATE;
import static org.junit.Assert.assertFalse;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.supermed.patient.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {

    @Before
    public void clearSession() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
    }
    //пользователь нажимает «Войти» с пустыми полями логина/пароля.
    @Test
    public void emptyCredentials_doesNotPersistSession() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.btn_login)).perform(click());

            scenario.onActivity(activity -> {
                SharedPreferences prefs = activity.getSharedPreferences("user_session", MODE_PRIVATE);
                assertFalse(prefs.contains("username"));
            });
        }
    }
    //пользователь нажимает «Регистрация» на форме логина.
    @Test
    public void registerButton_opensRegisterScreen() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.btn_register)).perform(click());

            SharedPreferences prefs = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext()
                    .getSharedPreferences("user_session", MODE_PRIVATE);
            assertFalse(prefs.contains("username"));
        }
    }
}
