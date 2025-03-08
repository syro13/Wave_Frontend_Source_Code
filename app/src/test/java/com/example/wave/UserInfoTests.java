package com.example.wave;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserInfoTests {

    private UserInfo userInfo;

    @Before
    public void setUp() {
        userInfo = new UserInfo();
    }

    @Test // Test the getFullName() method
    public void testGetFullName() {
        assertNull(userInfo.getFullName());
    }

    @Test  // Test the getFullName() method with a value
    public void testGetFullNameWithValue() {
        // Using reflection to set a value for the private field.
        try {
            java.lang.reflect.Field field = UserInfo.class.getDeclaredField("fullName");
            field.setAccessible(true);
            field.set(userInfo, "John Doe");
            assertEquals("John Doe", userInfo.getFullName());
        } catch (Exception e) {
            fail("Exception during reflection: " + e.getMessage());
        }
    }
}
