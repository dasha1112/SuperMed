package com.supermed;

import com.supermed.entities.Statistic;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelStatisticsTest {

    private Model model;

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    private MockedStatic<DatabaseManager> mockedDatabaseManager;

    @BeforeEach
    void setUp() throws SQLException {
        model = new Model();
        mockedDatabaseManager = mockStatic(DatabaseManager.class);
        mockedDatabaseManager.when(DatabaseManager::getConnection).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        lenient().when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    }

    private void mockStatisticResultSet() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("patient_username")).thenReturn("patientA");
        when(mockResultSet.getInt("doctor_id")).thenReturn(1);
        when(mockResultSet.getString("appointment_date")).thenReturn("2025-01-20");
        when(mockResultSet.getString("start_time")).thenReturn("10:00");
        when(mockResultSet.getString("end_time")).thenReturn("10:30");
        when(mockResultSet.getString("secret_id")).thenReturn("SEC001");
        when(mockResultSet.getString("status")).thenReturn("scheduled");
        when(mockResultSet.getString("doctor_name")).thenReturn("Иванов Иван Алексеевич");
        when(mockResultSet.getString("doctor_specialization")).thenReturn("Кардиолог");
        when(mockResultSet.getString("branch_name")).thenReturn("Центральный филиал");
        when(mockResultSet.getString("branch_address")).thenReturn("ул. Пушкина");
        when(mockResultSet.getString("doctor_schedule_day_of_week")).thenReturn("Понедельник");
        when(mockResultSet.getString("doctor_schedule_start_time")).thenReturn("09:00");
        when(mockResultSet.getString("doctor_schedule_end_time")).thenReturn("17:00");
    }

    @Test
    @DisplayName("getDetailedAppointmentsReport: get statistic without filter")
    void testGetDetailedAppointmentsReportNoFilters() throws SQLException {
        mockStatisticResultSet();

        List<Statistic> stats = model.getDetailedAppointmentsReport(null, null, null, null);

        assertNotNull(stats);
        assertFalse(stats.isEmpty());
        assertEquals(1, stats.size());
        assertEquals("patientA", stats.get(0).getPatientUsername());
        assertEquals("Кардиолог", stats.get(0).getDoctorSpecialization());
        assertEquals("Понедельник 09:00-17:00", stats.get(0).getFormattedDoctorSchedule());
        verify(mockPreparedStatement, never()).setObject(anyInt(), any());
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    @DisplayName("getDetailedAppointmentsReport: get statistic with filter doctorId")
    void testGetDetailedAppointmentsReportWithDoctorFilter() throws SQLException {
        mockStatisticResultSet();

        List<Statistic> stats = model.getDetailedAppointmentsReport(1, null, null, null);

        assertNotNull(stats);
        assertFalse(stats.isEmpty());
        assertEquals(1, stats.size());
        assertEquals("Иванов Иван Алексеевич", stats.get(0).getDoctorName());
        verify(mockPreparedStatement).setObject(1, 1);
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    @DisplayName("getDetailedAppointmentsReport: get statistic with filter branchId")
    void testGetDetailedAppointmentsReportWithBranchFilter() throws SQLException {
        mockStatisticResultSet();

        List<Statistic> stats = model.getDetailedAppointmentsReport(null, 1, null, null);

        assertNotNull(stats);
        assertFalse(stats.isEmpty());
        assertEquals(1, stats.size());
        assertEquals("Центральный филиал", stats.get(0).getBranchName());
        verify(mockPreparedStatement).setObject(1, 1);
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    @DisplayName("getDetailedAppointmentsReport: get statistic with filter startDate and endDate")
    void testGetDetailedAppointmentsReportWithDateFilters() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("patient_username")).thenReturn("patientA", "patientB");
        when(mockResultSet.getInt("doctor_id")).thenReturn(1, 1);
        when(mockResultSet.getString("appointment_date")).thenReturn("2025-01-20", "2025-01-21");
        when(mockResultSet.getString("start_time")).thenReturn("10:00", "09:00");
        when(mockResultSet.getString("end_time")).thenReturn("10:30", "09:30");
        when(mockResultSet.getString("secret_id")).thenReturn("SEC001", "SEC002");
        when(mockResultSet.getString("status")).thenReturn("scheduled", "scheduled");
        when(mockResultSet.getString("doctor_name")).thenReturn("Иванов Иван Алексеевич", "Иванов Иван Алексеевич");
        when(mockResultSet.getString("doctor_specialization")).thenReturn("Кардиолог", "Кардиолог");
        when(mockResultSet.getString("branch_name")).thenReturn("Центральный филиал", "Центральный филиал");
        when(mockResultSet.getString("branch_address")).thenReturn("ул. Пушкина", "ул. Пушкина");
        when(mockResultSet.getString("doctor_schedule_day_of_week")).thenReturn("Понедельник", "Вторник");
        when(mockResultSet.getString("doctor_schedule_start_time")).thenReturn("09:00", "09:00");
        when(mockResultSet.getString("doctor_schedule_end_time")).thenReturn("17:00", "17:00");

        List<Statistic> stats = model.getDetailedAppointmentsReport(null, null, "2025-01-20", "2025-01-21");

        assertNotNull(stats);
        assertFalse(stats.isEmpty());
        assertEquals(2, stats.size());
        verify(mockPreparedStatement).setObject(1, "2025-01-20");
        verify(mockPreparedStatement).setObject(2, "2025-01-21");
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    @DisplayName("getDetailedAppointmentsReport: get empty list with filter")
    void testGetDetailedAppointmentsReportEmpty() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);

        List<Statistic> stats = model.getDetailedAppointmentsReport(999, 999, "2020-01-01", "2020-01-01");

        assertNotNull(stats);
        assertTrue(stats.isEmpty());
        verify(mockPreparedStatement).setObject(1, 999);
        verify(mockPreparedStatement).setObject(2, 999);
        verify(mockPreparedStatement).setObject(3, "2020-01-01");
        verify(mockPreparedStatement).setObject(4, "2020-01-01");
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    @DisplayName("getDetailedAppointmentsReport: test SQLException")
    void testGetDetailedAppointmentsReportSQLException() throws SQLException {
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("DB error during report generation"));

        List<Statistic> stats = model.getDetailedAppointmentsReport(null, null, null, null);

        assertNotNull(stats);
        assertTrue(stats.isEmpty());
        verify(mockPreparedStatement).executeQuery();
    }


    @AfterEach
    void tearDown() {
        mockedDatabaseManager.close();
    }
}