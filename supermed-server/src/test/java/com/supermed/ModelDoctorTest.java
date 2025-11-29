package com.supermed;

import com.supermed.entities.Doctor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelDoctorTest {

    private Model model;

    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockResultSet;

    private MockedStatic<DatabaseManager> mockedDatabaseManager;

    @BeforeEach
    void setUp() throws SQLException {
        model = new Model();
        mockedDatabaseManager = mockStatic(DatabaseManager.class);
        mockedDatabaseManager.when(DatabaseManager::getConnection).thenReturn(mockConnection);

        when(mockConnection.createStatement()).thenReturn(mockStatement);
    }

    @Test
    @DisplayName("getAllDoctors: Add doctors")
    void testGetAllDoctorsSuccess() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("name")).thenReturn("Иванов", "Петров");
        when(mockResultSet.getString("specialization")).thenReturn("Кардиолог", "Терапевт");
        when(mockResultSet.getInt("branch_id")).thenReturn(1, 2);
        when(mockResultSet.getString("branch_name")).thenReturn("Центральный", "Северный");
        when(mockResultSet.getString("branch_address")).thenReturn("Ул. А", "Ул. Б");

        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        List<Doctor> doctors = model.getAllDoctors();

        assertNotNull(doctors);
        assertFalse(doctors.isEmpty());
        assertEquals(2, doctors.size());
        assertEquals("Иванов", doctors.get(0).getName());
        assertEquals("Кардиолог", doctors.get(0).getSpecialization());
        assertEquals("Центральный", doctors.get(0).getBranchName());
        assertEquals("Петров", doctors.get(1).getName());

        verify(mockStatement).executeQuery(anyString());
    }

    @Test
    @DisplayName("getAllDoctors: Add empty list")
    void testGetAllDoctorsEmpty() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);

        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        List<Doctor> doctors = model.getAllDoctors();

        assertNotNull(doctors);
        assertTrue(doctors.isEmpty());

        verify(mockStatement).executeQuery(anyString());
    }

    @Test
    @DisplayName("getAllDoctors: test SQLException")
    void testGetAllDoctorsSQLException() throws SQLException {
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("DB error"));

        List<Doctor> doctors = model.getAllDoctors();

        assertNotNull(doctors);
        assertTrue(doctors.isEmpty());
    }

    @AfterEach
    void tearDown() {
        mockedDatabaseManager.close();
    }
}