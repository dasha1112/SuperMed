package com.supermed.patient.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;

import org.junit.Test;

public class JsonMappingTest {

    private final Gson gson = new Gson();

    @Test
    public void schedule_withoutBranchName_parsesCoreFields() {
        String json = "{"
                + "\"id\":5,"
                + "\"doctorId\":2,"
                + "\"doctorName\":\"Dr. Ivanov\","
                + "\"dayOfWeek\":\"Понедельник\","
                + "\"startTime\":\"09:00\","
                + "\"endTime\":\"10:00\""
                + "}";

        Schedule schedule = gson.fromJson(json, Schedule.class);

        assertEquals(5, schedule.getId());
        assertEquals(2, schedule.getDoctorId());
        assertEquals("Dr. Ivanov", schedule.getDoctorName());
        assertEquals("Понедельник", schedule.getDayOfWeek());
        assertNull(schedule.getBranchName());
    }

    @Test
    public void appointment_parsesAllFields() {
        String json = "{"
                + "\"id\":12,"
                + "\"patientUsername\":\"p.kotova\","
                + "\"doctorId\":3,"
                + "\"appointmentDate\":\"2025-02-11\","
                + "\"startTime\":\"14:00\","
                + "\"endTime\":\"14:30\","
                + "\"secretId\":\"SEC777\","
                + "\"status\":\"scheduled\","
                + "\"doctorName\":\"Dr. Smith\""
                + "}";

        Appointment appointment = gson.fromJson(json, Appointment.class);

        assertEquals(12, appointment.getId());
        assertEquals("p.kotova", appointment.getPatientUsername());
        assertEquals(3, appointment.getDoctorId());
        assertEquals("2025-02-11", appointment.getAppointmentDate());
        assertEquals("14:00", appointment.getStartTime());
        assertEquals("SEC777", appointment.getSecretId());
        assertEquals("scheduled", appointment.getStatus());
        assertEquals("Dr. Smith", appointment.getDoctorName());
        assertEquals("2025-02-11 14:00", appointment.getDisplayTime());
    }
}
