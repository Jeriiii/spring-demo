package com.example.demo.controller;

import com.example.demo.Demo3Application;
import com.example.demo.model.Book;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

//////////////
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
///////////////

import java.util.regex.Matcher;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Demo3Application.class) //SpringApplicationConfiguration no longer exists
@WebAppConfiguration
public class ReadingListTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setupMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity()) //for test with login
                .build();
    }

    @Test
    public void homePage_unauthenticatedUser() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost/login"));
    }

    @Test
    public void homePage() throws Exception {
        mockMvc.perform(get("/home/readingList"))
                .andExpect(status().isOk())
                .andExpect(view().name("readingList"))
                .andExpect(model().attributeExists("books"));
                //.andExpect(model().attribute("books", is(empty()))); // Transaction manager - https://stackoverflow.com/questions/3646661/clearing-entire-database-for-unit-testing-with-hibernate
    }

    @Test
    @Rollback(true)
    public void postBook() throws Exception{
        Book expectedBook = new Book();
        expectedBook.setId(1L);
        expectedBook.setReader("readingList");
        expectedBook.setTitle("BOOK TITLE");
        expectedBook.setAuthor("BOOK AUTHOR");
        expectedBook.setIsbn("1234567890");
        expectedBook.setDescription("DESCRIPTION");
        expectedBook.setName("BOOK NAME");

        mockMvc.perform(
            post("/home/" + expectedBook.getReader())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", expectedBook.getTitle())
                .param("author", expectedBook.getAuthor())
                .param("isbn", expectedBook.getIsbn())
                .param("description", expectedBook.getDescription())
                .param("name", expectedBook.getName())
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", "/home/" + expectedBook.getReader()));

        mockMvc.perform(get("/home/readingList"))
                .andExpect(status().isOk())
                .andExpect(view().name("readingList"))
                .andExpect(model().attributeExists("books"))
                //.andExpect(model().attribute("books", hasSize(1))) //// Transaction manager - https://stackoverflow.com/questions/3646661/clearing-entire-database-for-unit-testing-with-hibernate
                .andExpect(model().attribute("books", everyItem(
                        hasProperty("author", is(expectedBook.getAuthor()))
                        //samePropertyValuesAs() - but id will be different
                )));
    }

}
