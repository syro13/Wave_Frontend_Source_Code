package com.example.wave;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

public class PromptsAdapterTest {
    private PromptsAdapter promptsAdapter;
    private List<String> promptsList;
    private PromptsAdapter.OnPromptClickListener listener;
    private Context context;

    @Before
    public void setUp() {
        // Setup mock context and mock listener
        context = mock(Context.class);
        listener = mock(PromptsAdapter.OnPromptClickListener.class);
        promptsList = Arrays.asList("Prompt 1", "Prompt 2", "Prompt 3");
        promptsAdapter = new PromptsAdapter(context, promptsList, listener);
    }

//    @Test // Tests Get item count method
//    public void testGetItemCount() {
//        assertEquals(3, promptsAdapter.getItemCount());
//    }
}
