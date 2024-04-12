package com.mgrtech.bookCatalog.api;

import com.mgrtech.bookCatalog.persistence.Book;

public class BookConverter {

    public BookDto toDto(Book book) {
        if(book == null) {
            return null;
        }
        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getGenre(),
                book.getCreatedOn(),
                book.getLastUpdateOn()
        );
    }

    public Book toBook(CreateBook request) {
        return new Book(request.title(), request.genre());
    }
}
