package com.mgrtech.bookCatalog.api;

import com.mgrtech.bookCatalog.persistence.Book;
import java.lang.reflect.Field;
import java.time.Instant;

public class BookMother {

    public static Book createBook(Long id, String title, String genre) throws NoSuchFieldException, IllegalAccessException {
        Book book = new Book(title, genre);

        Field idField = Book.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(book, id);

        Instant createdOn = Instant.now();
        Field createOnField = Book.class.getDeclaredField("createdOn");
        createOnField.setAccessible(true);
        createOnField.set(book, createdOn);

        Instant lastUpdateOn = Instant.now();
        Field lastUpdateOnField = Book.class.getDeclaredField("lastUpdateOn");
        lastUpdateOnField.setAccessible(true);
        lastUpdateOnField.set(book, lastUpdateOn);

        return book;
    }

}
