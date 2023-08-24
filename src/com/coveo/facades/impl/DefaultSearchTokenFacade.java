package com.coveo.facades.impl;

import com.coveo.SearchTokenBody;
import com.coveo.SearchTokenUserId;
import com.coveo.SearchTokenWsDTO;
import com.coveo.constants.CoveoccConstants;
import com.coveo.facades.SearchTokenFacade;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.site.BaseSiteService;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class DefaultSearchTokenFacade implements SearchTokenFacade {

    public static final String SEARCH_TOKEN_PATH = "/rest/search/v2/token";

    @Resource(name="baseSiteService")
    private BaseSiteService baseSiteService;

    @Override
    public ResponseEntity getSearchToken(String baseSiteId , String userId, String searchHub, long maxAgeMilliseconds) {

        BaseSiteModel baseSite = baseSiteService.getBaseSiteForUID(baseSiteId);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = buildHeaders(baseSite.getCoveoApiKey());

        SearchTokenBody searchTokenBody = buildRequestBody(userId, searchHub);

        URI uri = URI.create(baseSite.getCoveoPlatformUrl() + SEARCH_TOKEN_PATH);

        HttpEntity<SearchTokenBody> requestEntity = new HttpEntity<>(searchTokenBody, headers);
        ResponseEntity<SearchTokenWsDTO> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, SearchTokenWsDTO.class);

        return responseEntity;
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
        searchTokenBody.setUserIds(Arrays.asList(user));
        searchTokenBody.setSearchHub(searchHub);
        searchTokenBody.setValidFor(TimeUnit.SECONDS.toMillis(CoveoccConstants.SEARCH_TOKEN_MAX_AGE_SECONDS));

        return searchTokenBody;
    }
}
