package com.mgrtech.bookCatalog.api;

import java.time.Instant;

record BookDto(Long id, String title, String genre, Instant createdOn, Instant lastUpdateOn) { }
