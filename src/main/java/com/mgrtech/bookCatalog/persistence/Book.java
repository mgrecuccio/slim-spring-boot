package com.mgrtech.bookCatalog.persistence;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "BOOK")
public class Book {

    private Book() {
        //jpa purposes
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "GENRE", nullable = false)
    private String genre;
    @Column(name = "CREATED_ON")
    @CreationTimestamp
    private Instant createdOn;

    @Column(name = "LAST_UPDATE_ON")
    @UpdateTimestamp
    private Instant lastUpdateOn;

    public Book(String title, String genre) {
        this.title = title;
        this.genre = genre;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public Instant getLastUpdateOn() {
        return lastUpdateOn;
    }

}
