package com.mgrtech.bookCatalog.api;

import com.mgrtech.bookCatalog.persistence.Book;
import com.mgrtech.bookCatalog.persistence.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.stream.Collectors;

@RestController
public class BookController {

    private final Logger LOGGER = LoggerFactory.getLogger(BookController.class);

    private final BookRepository bookRepository;
    private final BookConverter bookConverter;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
        bookConverter = new BookConverter();
    }

    @GetMapping("/books")
    public ResponseEntity getBooks() {
        LOGGER.info("Retrieving all the books");
        return ResponseEntity.ok(bookRepository
                .findAll()
                .stream()
                .map(bookConverter::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/books/{id}")
    public ResponseEntity getBook(@PathVariable Long id) {
        LOGGER.info("Retrieving the book avec id=[{}]", id);
        BookDto bookDto = bookRepository.findById(id)
                .map(bookConverter::toDto)
                .orElse(null);
        return ResponseEntity.ofNullable(bookDto);
    }

    @PostMapping("books")
    public ResponseEntity addBook(@RequestBody CreateBook request){
        LOGGER.info("Creating the new book=[{}]", request);
        Book createdBook = bookRepository.save(bookConverter.toBook(request));

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdBook.getId())
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    @DeleteMapping("books/{id}")
    public ResponseEntity deleteBook(@PathVariable Long id) {
        LOGGER.info("Deleting the book with id=[{}]", id);
        if(bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
