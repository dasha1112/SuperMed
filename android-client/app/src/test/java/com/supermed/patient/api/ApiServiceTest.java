package com.supermed.patient.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.supermed.patient.model.Appointment;
import com.supermed.patient.model.AuthRequest;
import com.supermed.patient.model.AuthResponse;
import com.supermed.patient.model.Branch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiServiceTest {

    static {
        java.util.logging.Logger.getLogger(okhttp3.mockwebserver.MockWebServer.class.getName())
                .setLevel(java.util.logging.Level.OFF);
    }

    private MockWebServer server;
    private ApiService service;
    private Gson gson;

    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();

        gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        service = retrofit.create(ApiService.class);
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }
    //POST /auth/login сервер вернул статус 200; тело JSON вида { "user": { "username": "testUser", ... } }
    @Test
    public void login_successfulResponse_returnsUser() throws Exception {
        AuthRequest request = new AuthRequest("p.kotova", "patient123", "PATIENT");
        String body = "{ \"success\": true, \"message\": \"ok\", \"user\": {\"id\":1,\"username\":\"p.kotova\",\"userType\":\"PATIENT\",\"createdAt\":\"2025-01-10\"} }";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        Response<AuthResponse> response = service.login(request).execute();

        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertTrue(response.body().isSuccess());
        assertEquals("p.kotova", response.body().getUser().getUsername());

        RecordedRequest recorded = server.takeRequest();
        assertEquals("/auth/login", recorded.getPath());
    }
    //сервер вернул статус 500
    @Test
    public void login_serverError_returnsUnsuccessful() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500));

        Response<AuthResponse> response = service.login(new AuthRequest("u", "p", "PATIENT")).execute();

        assertFalse(response.isSuccessful());
    }
    //Сервер возвращает на GET /branches JSON с 2 объектами филиалов
    @Test
    public void getBranches_parsesList() throws Exception {
        String body = "[{\"id\":1,\"name\":\"Clinic A\",\"address\":\"Address 1\"},{\"id\":2,\"name\":\"Clinic B\",\"address\":\"Address 2\"}]";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        Response<List<Branch>> response = service.getBranches().execute();

        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertEquals(2, response.body().size());
        assertEquals("Clinic A", response.body().get(0).getName());
    }
    //POST /appointments статус 201 Created
    @Test
    public void createAppointment_sendsPayload() throws Exception {
        Appointment appointment = new Appointment(
                0,
                "p.kotova",
                2,
                "2025-02-10",
                "10:00",
                "10:30",
                "SEC123",
                "scheduled",
                "Dr. Ivanov"
        );
        server.enqueue(new MockResponse().setResponseCode(201));

        Response<Void> response = service.createAppointment(appointment).execute();

        assertTrue(response.isSuccessful());

        RecordedRequest request = server.takeRequest();
        assertEquals("/appointments", request.getPath());
        String sentJson = request.getBody().readUtf8();
        Appointment sent = gson.fromJson(sentJson, Appointment.class);
        assertEquals("p.kotova", sent.getPatientUsername());
        assertEquals(2, sent.getDoctorId());
        assertEquals("2025-02-10", sent.getAppointmentDate());
        assertEquals("SEC123", sent.getSecretId());
    }
    //POST /auth/register сервер вернул статус 201 Created
    @Test
    public void register_successfulResponse() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"success\":true,\"message\":\"registered\",\"user\":{\"username\":\"new.user\"}}"));

        Response<AuthResponse> response = service.register(new AuthRequest("new.user", "pw", "PATIENT")).execute();

        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertTrue(response.body().isSuccess());
        assertEquals("new.user", response.body().getUser().getUsername());

        RecordedRequest request = server.takeRequest();
        assertEquals("/auth/register", request.getPath());
    }
    //Сервер возвращает на GET /appointments массив JSON с приёмами: id, patientId, doctorId, date, start, end, secretId
    @Test
    public void getAppointments_parsesCollection() throws Exception {
        String body = "[{\"id\":1,\"patientUsername\":\"p.kotova\",\"doctorId\":2,\"appointmentDate\":\"2025-02-10\",\"startTime\":\"10:00\",\"endTime\":\"10:30\",\"secretId\":\"SEC123\",\"status\":\"scheduled\",\"doctorName\":\"Dr. Ivanov\"}]";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        Response<List<Appointment>> response = service.getAppointments().execute();

        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertEquals(1, response.body().size());
        assertEquals("SEC123", response.body().get(0).getSecretId());
    }

}




