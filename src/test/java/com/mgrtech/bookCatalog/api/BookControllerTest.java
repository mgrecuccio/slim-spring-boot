package com.mgrtech.bookCatalog.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgrtech.bookCatalog.persistence.Book;
import com.mgrtech.bookCatalog.persistence.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Optional;

import static com.mgrtech.bookCatalog.api.BookMother.createBook;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookRepository bookRepository;

    @Test
    void getBooksTest() throws Exception {
        when(bookRepository.findAll()).thenReturn(List.of(createBook(1L, "title", "genre")));

        mvc.perform(MockMvcRequestBuilders
                        .get("/books")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[*].id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[*].title").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[*].genre").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[*].createdOn").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[*].lastUpdateOn").isNotEmpty());

        verify(bookRepository).findAll();
    }

    @Test
    void getBookTest() throws Exception {
        Long id = 1L;
        when(bookRepository.findById(id)).thenReturn(Optional.of(createBook(id, "title", "genre")));

        mvc.perform(MockMvcRequestBuilders
                        .get("/books/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.genre").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdOn").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastUpdateOn").isNotEmpty());

        verify(bookRepository).findById(id);
    }

    @Test
    void getBookNotFoundTest() throws Exception {
        Long id = 1L;
        Long wrongId = 2L;
        when(bookRepository.findById(id)).thenReturn(Optional.of(createBook(id, "title", "genre")));

        mvc.perform(MockMvcRequestBuilders
                        .get("/books/{id}", wrongId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(bookRepository).findById(wrongId);
    }

    @Test
    void addBookTest() throws Exception {
        CreateBook request = new CreateBook("title", "genre");
        Book createdBook = createBook(1L, "title", "genre");
        when(bookRepository.save(any(Book.class))).thenReturn(createdBook);

        mvc.perform(MockMvcRequestBuilders
                        .post("/books")
                        .content(asJsonString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void deleteBookTest() throws Exception {
        Long id = 1L;
        when(bookRepository.existsById(id)).thenReturn(true);
        mvc.perform(MockMvcRequestBuilders
                        .delete("/books/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(bookRepository).existsById(id);
        verify(bookRepository).deleteById(id);
    }

    @Test
    void deleteBookNotFoundTest() throws Exception {
        Long id = 1L;
        mvc.perform(MockMvcRequestBuilders
                        .delete("/books/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookRepository).existsById(id);
        verify(bookRepository, times(0)).deleteById(id);
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}