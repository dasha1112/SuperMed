package com.supermed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HashTest {

    @Test
    @DisplayName("Hashing the same password")
    void testHashPasswordConsistency() {
        String password = "testPassword123";
        String hashedPassword1 = DatabaseManager.hashPassword(password);
        String hashedPassword2 = DatabaseManager.hashPassword(password);

        assertNotNull(hashedPassword1, "The hash must not be null");
        assertFalse(hashedPassword1.isEmpty(), "The hash must not be empty");
        assertEquals(hashedPassword1, hashedPassword2, "Identical passwords must hash the same way");
    }

    @Test
    @DisplayName("Hashing different passwords")
    void testHashPasswordDifference() {
        String passwordOne = "testPassword123";
        String passwordTwo = "anotherPassword456";
        String hashedPasswordOne = DatabaseManager.hashPassword(passwordOne);
        String hashedPasswordTwo = DatabaseManager.hashPassword(passwordTwo);

        assertNotEquals(hashedPasswordOne, hashedPasswordTwo, "Different passwords should be hashed differently");
    }
}