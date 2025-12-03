package com.supermed;

import com.supermed.entities.Branch;
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
class ModelBranchTest {

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
    @DisplayName("getAllBranches: get list of all branches")
    void testGetAllBranchesSuccess() throws SQLException {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("id")).thenReturn(1, 2);
        when(mockResultSet.getString("name")).thenReturn("Центральный", "Северный");
        when(mockResultSet.getString("address")).thenReturn("Ул. Пушкина", "Ул. Горького");

        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        List<Branch> branches = model.getAllBranches();

        assertNotNull(branches);
        assertFalse(branches.isEmpty());
        assertEquals(2, branches.size());
        assertEquals("Центральный", branches.get(0).getName());
        assertEquals("Северный", branches.get(1).getName());

        verify(mockStatement).executeQuery(anyString());
    }

    @Test
    @DisplayName("getAllBranches: get empty list of branches")
    void testGetAllBranchesEmpty() throws SQLException {
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockResultSet.next()).thenReturn(false);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        List<Branch> branches = model.getAllBranches();

        assertNotNull(branches);
        assertTrue(branches.isEmpty());
    }

    @Test
    @DisplayName("getBranchById: get branch by id")
    void testGetBranchByIdSuccess() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("name")).thenReturn("Центральный");
        when(mockResultSet.getString("address")).thenReturn("Ул. Пушкина");

        Branch branch = model.getBranchById(1);

        assertNotNull(branch);
        assertEquals(1, branch.getId());
        assertEquals("Центральный", branch.getName());

        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeQuery();
    }

    @AfterEach
    void tearDown() {
        mockedDatabaseManager.close();
    }
}