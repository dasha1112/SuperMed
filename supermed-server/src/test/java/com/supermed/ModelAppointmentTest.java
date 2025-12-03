package com.supermed;

import com.supermed.entities.Appointment;

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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelAppointmentTest {

    private Model model;

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
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
    }

    @Test
    @DisplayName("getAllAppointments: Get appointments list")
    void testGetAllAppointmentsSuccess() throws SQLException {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("patient_username")).thenReturn("p.kotova");
        when(mockResultSet.getInt("doctor_id")).thenReturn(1);
        when(mockResultSet.getString("appointment_date")).thenReturn("2025-01-20");
        when(mockResultSet.getString("start_time")).thenReturn("10:00");
        when(mockResultSet.getString("end_time")).thenReturn("10:30");
        when(mockResultSet.getString("secret_id")).thenReturn("SEC001");
        when(mockResultSet.getString("status")).thenReturn("scheduled");
        when(mockResultSet.getString("doctor_name")).thenReturn("Иванов Иван Алексеевич");
        List<Appointment> appointments = model.getAllAppointments();
        assertNotNull(appointments);
        assertFalse(appointments.isEmpty());
        assertEquals(1, appointments.size());
        assertEquals("p.kotova", appointments.get(0).getPatientUsername());
        assertEquals("Иванов Иван Алексеевич", appointments.get(0).getDoctorName());
        verify(mockStatement).executeQuery(anyString());
    }

    @Test
    @DisplayName("createAppointment")
    void testCreateAppointmentSuccess() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        Appointment newAppt = new Appointment(0, "new_patient", 1, "2025-02-01", "11:00", "11:30", "NEWSECID", "scheduled", "Dr. Test");
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = model.createAppointment(newAppt);

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, newAppt.getPatientUsername());
        verify(mockPreparedStatement).setInt(2, newAppt.getDoctorId());
        verify(mockPreparedStatement).setString(3, newAppt.getAppointmentDate());
        verify(mockPreparedStatement).setString(4, newAppt.getStartTime());
        verify(mockPreparedStatement).setString(5, newAppt.getEndTime());
        verify(mockPreparedStatement).setString(6, newAppt.getSecretId());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("getPatientAppointments: Get appointments list for patient")
    void testGetPatientAppointmentsSuccess() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("patient_username")).thenReturn("p.kotova");
        when(mockResultSet.getInt("doctor_id")).thenReturn(1);
        when(mockResultSet.getString("appointment_date")).thenReturn("2025-01-20");
        when(mockResultSet.getString("start_time")).thenReturn("10:00");
        when(mockResultSet.getString("end_time")).thenReturn("10:30");
        when(mockResultSet.getString("secret_id")).thenReturn("SEC001");
        when(mockResultSet.getString("status")).thenReturn("scheduled");
        when(mockResultSet.getString("doctor_name")).thenReturn("Иванов Иван Алексеевич");

        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<Appointment> appointments = model.getPatientAppointments("p.kotova");

        assertNotNull(appointments);
        assertFalse(appointments.isEmpty());
        assertEquals(1, appointments.size());
        assertEquals("p.kotova", appointments.get(0).getPatientUsername());

        verify(mockPreparedStatement).setString(1, "p.kotova");
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    @DisplayName("getPatientAppointments: Get empty list for not exist patient")
    void testGetPatientAppointmentsEmpty() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockResultSet.next()).thenReturn(false);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<Appointment> appointments = model.getPatientAppointments("non_existent_patient");

        assertNotNull(appointments);
        assertTrue(appointments.isEmpty());

        verify(mockPreparedStatement).setString(1, "non_existent_patient");
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    @DisplayName("getDoctorSchedule: Get schedule list for doctor")
    void testGetDoctorScheduleSuccess() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(true, false);

        when(mockResultSet.getInt("id"))
                .thenReturn(101);
        when(mockResultSet.getString("patient_username")).thenReturn("p.kotova");
        when(mockResultSet.getInt("doctor_id")).thenReturn(101);
        when(mockResultSet.getString("appointment_date")).thenReturn("2025-01-20");
        when(mockResultSet.getString("start_time")).thenReturn("09:00");
        when(mockResultSet.getString("end_time")).thenReturn("09:30");
        when(mockResultSet.getString("secret_id")).thenReturn("SEC001");
        when(mockResultSet.getString("status")).thenReturn("scheduled");
        when(mockResultSet.getString("doctor_name")).thenReturn("Иванов Иван Алексеевич");

        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet)
                .thenReturn(mockResultSet);

        List<Appointment> schedule = model.getDoctorSchedule("d.ivanov");

        assertNotNull(schedule);
        assertFalse(schedule.isEmpty());
        assertEquals(1, schedule.size());
        assertEquals("p.kotova", schedule.get(0).getPatientUsername());
        assertEquals("Иванов Иван Алексеевич", schedule.get(0).getDoctorName());

        verify(mockPreparedStatement, atLeastOnce()).setString(anyInt(), anyString());
        verify(mockPreparedStatement, times(2)).executeQuery();
    }

    @Test
    @DisplayName("getDoctorSchedule: Get empty list for not exist doctor")
    void testGetDoctorScheduleDoctorNotFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockResultSet.next()).thenReturn(false);

        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<Appointment> schedule = model.getDoctorSchedule("non_existent_doctor");

        assertNotNull(schedule);
        assertTrue(schedule.isEmpty());

        verify(mockPreparedStatement).setString(1, "non_existent_doctor");
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    @DisplayName("completeAppointment: successfully complete appointment")
    void testCompleteAppointmentSuccess() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = model.completeAppointment(1);

        assertTrue(result);
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("completeAppointment: not successfully complete appointment")
    void testCompleteAppointmentFailure() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        boolean result = model.completeAppointment(999);

        assertFalse(result);
    }

    @AfterEach
    void tearDown() {
        mockedDatabaseManager.close();
    }
}