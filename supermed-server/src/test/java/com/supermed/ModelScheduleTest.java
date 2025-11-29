package com.supermed;

import com.supermed.entities.Schedule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelScheduleTest {

    private Model model;

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;

    private MockedStatic<DatabaseManager> mockedDatabaseManager;

    @BeforeEach
    void setUp() throws SQLException {
        model = new Model();
        mockedDatabaseManager = mockStatic(DatabaseManager.class);
        mockedDatabaseManager.when(DatabaseManager::getConnection).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    }

    @Test
    @DisplayName("updateSchedule: successfully update schedule")
    void testUpdateScheduleSuccess() throws SQLException {
        Schedule scheduleToUpdate = new Schedule(1, 1, "Иванов", "Вторник", "10:00", "18:00");
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = model.updateSchedule(scheduleToUpdate);

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, "Вторник");
        verify(mockPreparedStatement).setString(2, "10:00");
        verify(mockPreparedStatement).setString(3, "18:00");
        verify(mockPreparedStatement).setInt(4, 1);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("updateSchedule: not successfully update schedule")
    void testUpdateScheduleNotFound() throws SQLException {
        Schedule scheduleToUpdate = new Schedule(999, 1, "Иванов", "Вторник", "10:00", "18:00");
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        boolean result = model.updateSchedule(scheduleToUpdate);

        assertFalse(result);
    }

    @Test
    @DisplayName("addSchedule: successfully add schedule")
    void testAddScheduleSuccess() throws SQLException {
        Schedule newSchedule = new Schedule(0, 1, "Иванов", "Среда", "09:00", "17:00");
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = model.addSchedule(newSchedule);

        assertTrue(result);
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).setString(2, "Среда");
        verify(mockPreparedStatement).setString(3, "09:00");
        verify(mockPreparedStatement).setString(4, "17:00");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("addSchedule: not successfully add schedule")
    void testAddScheduleSQLException() throws SQLException {
        Schedule newSchedule = new Schedule(0, 1, "Иванов", "Среда", "09:00", "17:00");
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("DB insert error"));

        boolean result = model.addSchedule(newSchedule);

        assertFalse(result);
    }

    @Test
    @DisplayName("deleteSchedule: successfully delete schedule")
    void testDeleteScheduleSuccess() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = model.deleteSchedule(1);

        assertTrue(result);
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("deleteSchedule: not successfully delete schedule")
    void testDeleteScheduleNotFound() throws SQLException {
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        boolean result = model.deleteSchedule(999);

        assertFalse(result);
    }

    @AfterEach
    void tearDown() {
        mockedDatabaseManager.close();
    }
}