package com.mgrtech.bookCatalog.api;

import com.mgrtech.bookCatalog.persistence.Book;
import org.junit.jupiter.api.Test;

import static com.mgrtech.bookCatalog.api.BookMother.createBook;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BookConverterTest {

    private final BookConverter converter = new BookConverter();

    @Test
    void toDtoConversionTest() throws NoSuchFieldException, IllegalAccessException {
        Book book = createBook(1L, "title", "genre");

        BookDto dto = converter.toDto(book);

        assertEquals(dto.id(), book.getId());
        assertEquals(dto.title(), book.getTitle());
        assertEquals(dto.genre(), book.getGenre());
        assertEquals(dto.createdOn(), book.getCreatedOn());
        assertEquals(dto.lastUpdateOn(), book.getLastUpdateOn());
    }

    @Test
    void toBookConversionTest() {
        CreateBook request = new CreateBook("title", "genre");
        Book book = converter.toBook(request);

        assertEquals(book.getTitle(), request.title());
        assertEquals(book.getGenre(), request.genre());
    }
}