package com.supermed;

import com.supermed.entities.Conversation;
import com.supermed.entities.Message;
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
class ModelMessageTest {

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
    }

    @Test
    @DisplayName("sendMessage: successfully send message")
    void testSendMessageSuccess() throws SQLException {
        Message msg = new Message(0, "sender", "receiver", "Hello", "now");
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean success = model.sendMessage(msg);

        assertTrue(success);
        verify(mockPreparedStatement).setString(1, "sender");
        verify(mockPreparedStatement).setString(2, "receiver");
        verify(mockPreparedStatement).setString(3, "Hello");
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("sendMessage: not successfully send message")
    void testSendMessageSQLException() throws SQLException {
        Message msg = new Message(0, "sender", "receiver", "Hello", "now");
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("DB error"));

        boolean success = model.sendMessage(msg);

        assertFalse(success);
    }

    @Test
    @DisplayName("getDoctorMessages: get all messages of doctor")
    void testGetDoctorMessagesSuccess() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("sender_username")).thenReturn("patient1", "doctor1");
        when(mockResultSet.getString("receiver_username")).thenReturn("doctor1", "patient1");
        when(mockResultSet.getString("message_text")).thenReturn("Hi doc", "Hi patient");
        when(mockResultSet.getString("timestamp")).thenReturn("2023-01-01", "2023-01-01");

        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<Message> messages = model.getDoctorMessages("doctor1");

        assertFalse(messages.isEmpty());
        assertEquals(2, messages.size());
        assertEquals("Hi doc", messages.get(0).getMessageText());
        assertEquals("Hi patient", messages.get(1).getMessageText());
        verify(mockPreparedStatement, times(2)).setString(anyInt(), eq("doctor1"));
    }

    @Test
    @DisplayName("getDoctorMessages: get empty list of doctor messages")
    void testGetDoctorMessagesEmpty() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<Message> messages = model.getDoctorMessages("no_messages_user");

        assertTrue(messages.isEmpty());
    }


    @Test
    @DisplayName("getDoctorConversations: get doctor conversations")
    void testGetDoctorConversationsSuccess() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("participant")).thenReturn("patient1");
        when(mockResultSet.getString("last_message_time")).thenReturn("2023-01-01 10:00:00");

        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<Conversation> conversations = model.getDoctorConversations("doctor1");

        assertFalse(conversations.isEmpty());
        assertEquals(1, conversations.size());
        assertEquals("patient1", conversations.get(0).getParticipant());
        verify(mockPreparedStatement, times(3)).setString(anyInt(), eq("doctor1"));
    }

    @Test
    @DisplayName("getDoctorConversations: get empty list of doctor conversations")
    void testGetDoctorConversationsEmpty() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<Conversation> conversations = model.getDoctorConversations("doctor_no_conv");

        assertTrue(conversations.isEmpty());
    }


    @Test
    @DisplayName("getConversationMessages: get messages of conversation")
    void testGetConversationMessagesSuccess() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("sender_username")).thenReturn("doctor1");
        when(mockResultSet.getString("receiver_username")).thenReturn("patient1");
        when(mockResultSet.getString("message_text")).thenReturn("How are you?");
        when(mockResultSet.getString("timestamp")).thenReturn("2023-01-01 10:30:00");

        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<Message> messages = model.getConversationMessages("doctor1", "patient1");

        assertFalse(messages.isEmpty());
        assertEquals(1, messages.size());
        assertEquals("How are you?", messages.get(0).getMessageText());
        verify(mockPreparedStatement).setString(1, "doctor1");
        verify(mockPreparedStatement).setString(2, "patient1");
        verify(mockPreparedStatement).setString(3, "patient1");
        verify(mockPreparedStatement).setString(4, "doctor1");
    }

    @Test
    @DisplayName("getConversationMessages: get empty list of messages of conversation")
    void testGetConversationMessagesEmpty() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        List<Message> messages = model.getConversationMessages("doctor1", "patient_no_msg");

        assertTrue(messages.isEmpty());
    }

    @AfterEach
    void tearDown() {
        mockedDatabaseManager.close();
    }
}