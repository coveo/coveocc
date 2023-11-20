package com.coveo.facades.impl;

import com.coveo.SearchTokenBody;
import com.coveo.SearchTokenUserId;
import com.coveo.SearchTokenWsDTO;
import com.coveo.constants.CoveoccConstants;
import com.coveo.facades.SearchTokenFacade;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.site.BaseSiteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DefaultSearchTokenFacade implements SearchTokenFacade {

    @Value("${coveocc.searchtoken.path}")
    private String searchTokenPath;

    @Resource(name="baseSiteService")
    private BaseSiteService baseSiteService;

    @Resource(name="searchTokenRestTemplate")
    private RestTemplate searchTokenRestTemplate;

    @Override
    public ResponseEntity<SearchTokenWsDTO> getSearchToken(String baseSiteId , String userId, String searchHub, long maxAgeMilliseconds) {

        BaseSiteModel baseSite = baseSiteService.getBaseSiteForUID(baseSiteId);

        HttpHeaders headers = buildHeaders(baseSite.getCoveoApiKey());

        SearchTokenBody searchTokenBody = buildRequestBody(userId, searchHub);

        URI uri = URI.create(baseSite.getCoveoPlatformUrl() + searchTokenPath);

        HttpEntity<SearchTokenBody> requestEntity = new HttpEntity<>(searchTokenBody, headers);
        return searchTokenRestTemplate.exchange(uri, HttpMethod.POST, requestEntity, SearchTokenWsDTO.class);
    }

    private HttpHeaders buildHeaders(String coveoApiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(coveoApiKey);
        return headers;
    }

    private static SearchTokenBody buildRequestBody(String userId, String searchHub) {
        SearchTokenBody searchTokenBody = new SearchTokenBody();
        SearchTokenUserId user = new SearchTokenUserId();
        user.setName(userId);
        user.setType("User");
        user.setProvider("Email Security Provider");
        searchTokenBody.setUserIds(List.of(user));
        searchTokenBody.setSearchHub(searchHub);
        searchTokenBody.setValidFor(TimeUnit.SECONDS.toMillis(CoveoccConstants.SEARCH_TOKEN_MAX_AGE_SECONDS));

        return searchTokenBody;
    }
}
