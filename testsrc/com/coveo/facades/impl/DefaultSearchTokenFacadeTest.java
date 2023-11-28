package com.coveo.facades.impl;

import com.coveo.SearchTokenBody;
import com.coveo.SearchTokenWsDTO;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.site.BaseSiteService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
public class DefaultSearchTokenFacadeTest {

    private static final String TEST_COVEO_BASE_URL = "https://localhost:666";
    private static final String TEST_COVEO_API_KEY = "test-api-key";
    private static final String TEST_BASE_SITE_ID = "testBaseSite";
    private static final String TEST_USER_ID = "testUserId";
    private static final String TEST_SEARCH_HUB = "testSearchHub";
    private static final String TEST_TOKEN = "testToken";
    private static final Long TEST_MAX_AGE_MS = 3000L;

    @InjectMocks
    private final DefaultSearchTokenFacade searchTokenFacade = new DefaultSearchTokenFacade();

    @Mock
    private BaseSiteService baseSiteService;

    @Mock
    private RestTemplate searchTokenRestTemplate;

    @Mock
    private BaseSiteModel mockBaseSite;

    @Captor
    private ArgumentCaptor<URI> uriCaptor;

    @Captor
    private ArgumentCaptor<HttpMethod> httpMethodCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity<SearchTokenBody>> requestEntityCaptor;

    @Captor
    private ArgumentCaptor<Class<SearchTokenWsDTO>> classCaptor;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(searchTokenFacade, "searchTokenPath", "/rest/search/v2/token");
        MockitoAnnotations.initMocks(this);
        when(mockBaseSite.getCoveoPlatformUrl()).thenReturn(TEST_COVEO_BASE_URL);
        when(mockBaseSite.getCoveoApiKey()).thenReturn(TEST_COVEO_API_KEY);
        when(baseSiteService.getBaseSiteForUID(TEST_BASE_SITE_ID)).thenReturn(mockBaseSite);

        final SearchTokenWsDTO token = new SearchTokenWsDTO();
        token.setToken(TEST_TOKEN);
        final ResponseEntity<SearchTokenWsDTO> responseEntity = new ResponseEntity<>(token, HttpStatus.OK);

        when(searchTokenRestTemplate.exchange(anyObject(), eq(HttpMethod.POST), anyObject(), eq(SearchTokenWsDTO.class))).thenReturn(responseEntity);
    }

    @Test
    public void testTokenGeneration() {
        ResponseEntity<SearchTokenWsDTO> tokenResponse = searchTokenFacade.getSearchToken(TEST_BASE_SITE_ID, TEST_USER_ID, TEST_SEARCH_HUB, TEST_MAX_AGE_MS);

        assertThat(tokenResponse.getBody()).isNotNull();
        assertThat(tokenResponse.getBody().getToken()).isEqualTo(TEST_TOKEN);

        verify(searchTokenRestTemplate).exchange(uriCaptor.capture(), httpMethodCaptor.capture(), requestEntityCaptor.capture(), classCaptor.capture());
        assertThat(uriCaptor.getValue()).hasToString(TEST_COVEO_BASE_URL + "/rest/search/v2/token");
        assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.POST);
        assertThat(classCaptor.getValue()).isEqualTo(SearchTokenWsDTO.class);

        final HttpEntity<SearchTokenBody> httpEntity = requestEntityCaptor.getValue();

        final List<String> auth = httpEntity.getHeaders().get("Authorization");
        assertThat(auth).isNotNull().hasSize(1);
        assertThat(auth.get(0)).contains(TEST_COVEO_API_KEY);

        final SearchTokenBody body = httpEntity.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getSearchHub()).isEqualTo(TEST_SEARCH_HUB);
        assertThat(body.getUserIds()).hasSize(1);
        assertThat(body.getUserIds().get(0).getName()).isEqualTo(TEST_USER_ID);
    }
}
