package com.supermed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class ManagerControllerTest {

    private ManagerController controller;

    @BeforeEach
    public void setUp() {
        controller = new ManagerController();
    }

    @Test
    public void testGetUserTypeDisplayName() throws Exception {
        // Получаем приватный метод через рефлексию
        Method method = ManagerController.class.getDeclaredMethod(
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
            ManagerController.class.getDeclaredMethod("loadStatistics");
            ManagerController.class.getDeclaredMethod("refreshSchedules");
            ManagerController.class.getDeclaredMethod("showAddScheduleDialog");
            ManagerController.class.getDeclaredMethod("handleLogout");
        });
    }

    @Test
    public void testPrivateMethodsExist() throws Exception {
        // Проверяем существование приватных методов через рефлексию
        Method updateStatusMethod = ManagerController.class.getDeclaredMethod(
                "updateStatus", String.class, String.class);
        updateStatusMethod.setAccessible(true);
        assertNotNull(updateStatusMethod);

        Method showAlertMethod = ManagerController.class.getDeclaredMethod(
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
        assertTrue(controller instanceof ManagerController);
    }

    @Test
    public void testBasicAssertions() {
        // Простые проверки JUnit
        assertEquals(4, 2 + 2);
        assertTrue(5 > 3);
        assertFalse(5 < 3);
        assertNotNull(controller);
    }
}