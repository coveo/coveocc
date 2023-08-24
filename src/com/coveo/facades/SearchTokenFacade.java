package com.coveo.facades;

import org.springframework.http.ResponseEntity;

public interface SearchTokenFacade {

    ResponseEntity getSearchToken(String baseSiteId, String userId, String searchHub, long maxAgeMilliseconds);
}
