package com.supermed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class DoctorControllerTest {

    private DoctorController controller;

    @BeforeEach
    public void setUp() {
        controller = new DoctorController();
    }

    @Test
    public void testGetUserTypeDisplayName() throws Exception {
        // Получаем приватный метод через рефлексию
        Method method = DoctorController.class.getDeclaredMethod(
                "getUserTypeDisplayName", String.class);
        method.setAccessible(true);

        // Проверяем все варианты
        assertEquals("Менеджер", method.invoke(controller, "MANAGER"));
        assertEquals("Врач", method.invoke(controller, "DOCTOR"));
        assertEquals("Пациент", method.invoke(controller, "PATIENT"));
        assertEquals("Пользователь", method.invoke(controller, "UNKNOWN"));
    }

    @Test
    public void testMethodsExist() throws Exception {
        // Проверяем, что все публичные методы существуют
        assertDoesNotThrow(() -> {
            DoctorController.class.getDeclaredMethod("refreshSchedule");
            DoctorController.class.getDeclaredMethod("handleShowToday");
            DoctorController.class.getDeclaredMethod("handleShowWeek");
            DoctorController.class.getDeclaredMethod("handleRefreshScheduleTab");
            DoctorController.class.getDeclaredMethod("loadWorkSchedule");
            DoctorController.class.getDeclaredMethod("refreshWorkSchedule");
            DoctorController.class.getDeclaredMethod("handleLogout");
            DoctorController.class.getDeclaredMethod("sendMessage");
        });
    }

    @Test
    public void testPrivateMethodsExist() throws Exception {
        // Проверяем существование приватных методов через рефлексию
        Method updateStatusMethod = DoctorController.class.getDeclaredMethod(
                "updateStatus", String.class, String.class);
        updateStatusMethod.setAccessible(true);
        assertNotNull(updateStatusMethod);

        Method showAlertMethod = DoctorController.class.getDeclaredMethod(
                "showAlert", String.class, String.class, String.class);
        showAlertMethod.setAccessible(true);
        assertNotNull(showAlertMethod);
    }

    @Test
    public void testControllerCreation() {
        assertNotNull(controller);
    }

    @Test
    public void testInstanceOf() {
        assertTrue(controller instanceof DoctorController);
    }
}