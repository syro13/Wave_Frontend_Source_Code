package com.example.wave;

import android.content.Context;
import android.widget.RatingBar;

import androidx.recyclerview.widget.RecyclerView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SchoolTasksBlogAdapterUnitTest {
    @Mock
    Context mockContext;

    private SchoolTasksBlogAdapter adapter;
    private List<BlogResponse> mockBlogList;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mockBlogList = new ArrayList<>();
        mockBlogList.add(new BlogResponse("Test Blog 1", "Author 1", "school-tasks", "url1", "imageUrl1"));
        mockBlogList.add(new BlogResponse("Test Blog 2", "Author 2", "school-tasks", "url2", "imageUrl2"));

        adapter = new SchoolTasksBlogAdapter(mockContext, mockBlogList);
    }

//    @Test Does not work
//    public void testGetItemCount() {
//        // Verify that the item count is correct
//        int itemCount = adapter.getItemCount();
//        assertEquals("Item count should match the size of the blog list", 2, itemCount);
//    }
}
