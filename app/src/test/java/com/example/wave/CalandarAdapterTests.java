package com.example.wave;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalandarAdapterTests {

    private CalendarAdapter adapter;
    private List<String> sampleDates;
    private Set<String> schoolTaskDates;
    private Set<String> homeTaskDates;

    @Before
    public void setUp() {
        sampleDates = Arrays.asList("01", "02", "03", "04", "05");
        schoolTaskDates = new HashSet<>(Arrays.asList("02", "04"));
        homeTaskDates = new HashSet<>(Arrays.asList("03"));

        adapter = new CalendarAdapter(sampleDates, date -> {}, schoolTaskDates, homeTaskDates);
    }

    @Test
    public void testGetItemCount() {
        assertEquals(5, adapter.getItemCount());
    }

//    @Test
//    public void testUpdateData() {
//        List<String> newDates = Arrays.asList("06", "07", "08");
//        adapter.updateData(newDates);
//        assertEquals(3, adapter.getItemCount());
//    }
}
