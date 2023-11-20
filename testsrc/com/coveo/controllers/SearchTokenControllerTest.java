/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.coveo.controllers;

import com.coveo.SearchTokenWsDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.hybris.platform.commercewebservices.core.constants.YcommercewebservicesConstants;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.impex.jalo.ImpExException;
import de.hybris.platform.oauth2.constants.OAuth2Constants;
import de.hybris.platform.servicelayer.ServicelayerTest;

import de.hybris.platform.webservicescommons.testsupport.client.WsRequestBuilder;
import de.hybris.platform.webservicescommons.testsupport.client.WsSecuredRequestBuilder;
import de.hybris.platform.webservicescommons.testsupport.server.NeedsEmbeddedServer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@NeedsEmbeddedServer(webExtensions = {YcommercewebservicesConstants.EXTENSIONNAME, OAuth2Constants.EXTENSIONNAME})
@IntegrationTest
public class SearchTokenControllerTest extends ServicelayerTest
{
	private static final int TOKEN_SERVICE_PORT = 8200;
	private static final String TOKEN_SERVICE_PATH = "/rest/search/v2/token";
	private static final String JWT_SECRET = "asdfSFS34wfsdfsdfSDSD32dfsddDDerQSNCK34SOWEK5354fdgdf4";
	private static final Key HMAC_KEY = new SecretKeySpec(Base64.getDecoder().decode(JWT_SECRET), SignatureAlgorithm.HS256.getJcaName());
	private static final String INVALID = "invalid";
	private static final String TEST_BASE_SITE = "testbasesite";
	private static final String TEST_SEARCH_HUB = "searchHub";
	private static final String JWT_ROLE = "role";
	private static final String JWT_PROVIDER = "provider";
	private static final String JWT_SOURCE = "source";
	private static final String TEST_USER = "test@email.com";
	private static final String TEST_USER_PASSWORD = "password";
	private static final String ANON_USER = "anonymous";
	private static final String SERVICE_VERSION = "v2";
	private static final String TOKEN_RESOURCE = "token";
	private static final String COVEO_PATH = "coveo";
	public static final String OAUTH_CLIENT_ID = "trusted_client";
	public static final String OAUTH_CLIENT_PASS = "secret";

	private static HttpServer httpServer;

	private WsSecuredRequestBuilder wsSecuredRequestBuilder;

	private static WsRequestBuilder wsRequestBuilder;

	@BeforeClass
	public static void initialSetUp() throws IOException {
		httpServer = createServer();
		httpServer.start();
	}

	@Before
	public void setUp() throws ImpExException {
		importCsv("/coveocc/testdata/searchcontroller-create-data.impex", "utf-8");

		wsSecuredRequestBuilder = new WsSecuredRequestBuilder()
				.extensionName(YcommercewebservicesConstants.EXTENSIONNAME)
				.client(OAUTH_CLIENT_ID, OAUTH_CLIENT_PASS)
				.grantResourceOwnerPasswordCredentials()
				.resourceOwner(TEST_USER, TEST_USER_PASSWORD)
				.path(SERVICE_VERSION)
				.path(TEST_BASE_SITE)
				.path(COVEO_PATH)
				.path(TOKEN_RESOURCE)
				.path(TEST_SEARCH_HUB);

		wsRequestBuilder = new WsRequestBuilder()
				.extensionName(YcommercewebservicesConstants.EXTENSIONNAME)
				.path(SERVICE_VERSION)
				.path(TEST_BASE_SITE)
				.path(COVEO_PATH)
				.path(TOKEN_RESOURCE)
				.path(TEST_SEARCH_HUB);
	}

	@After
	public void tearDown () throws ImpExException {
		importCsv("/coveocc/testdata/searchcontroller-remove-data.impex", "utf-8");
	}

	@AfterClass
	public static void finalTearDown() {
		httpServer.stop(0);
	}

	@Test
	public void testAnonymousTokenGeneration()
	{
		final Response wsResponse = wsRequestBuilder
				.build()
				.get();

		final SearchTokenWsDTO token = wsResponse.readEntity(SearchTokenWsDTO.class);
		final Jws<Claims> jwt = Jwts.parserBuilder()
				.setSigningKey(HMAC_KEY)
				.build()
				.parseClaimsJws(token.getToken());

		validateCommonValues(jwt);

		final String sub = jwt.getBody().getSubject();
		assertThat(sub).isEqualTo(ANON_USER);

	}

	@Test
	public void testSecuredTrustedUserTokenGeneration() throws IOException {
		final Response wsResponse = wsSecuredRequestBuilder
				.build()
				.get();

		final SearchTokenWsDTO token = wsResponse.readEntity(SearchTokenWsDTO.class);
		final Jws<Claims> jwt = Jwts.parserBuilder()
				.setSigningKey(HMAC_KEY)
				.build()
				.parseClaimsJws(token.getToken());

		validateCommonValues(jwt);

		final String sub = jwt.getBody().getSubject();
		assertThat(sub).isEqualTo(TEST_USER);
	}

	private static void validateCommonValues(final Jws<Claims> jwt) {
		final String role = (String) jwt.getBody().get(JWT_ROLE);
		final String provider = (String) jwt.getBody().get(JWT_PROVIDER);
		final String source = (String) jwt.getBody().get(JWT_SOURCE);
		assertThat(role).isEqualTo("User");
		assertThat(provider).isEqualTo("Email Security Provider");
		assertThat(source).isEqualTo(TEST_SEARCH_HUB);
	}

	private static HttpServer createServer() throws IOException {
		final HttpServer httpServer = HttpServer.create(new InetSocketAddress(TOKEN_SERVICE_PORT), 0);
		httpServer.createContext(TOKEN_SERVICE_PATH, new TokenHandler());
		return httpServer;
	}

	private static final class TokenHandler implements HttpHandler {

		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			try (final OutputStream os = exchange.getResponseBody(); final InputStream is = exchange.getRequestBody()) {
				final String result = IOUtils.toString(is, StandardCharsets.UTF_8);
				final ObjectMapper mapper = new ObjectMapper();
				final Map<String, Object> payloadMap = mapper.readValue(result, new TypeReference<Map<String, Object>>() {});

				String sub = null;
				String role = null;
				String provider = null;
				String source = (String) payloadMap.getOrDefault("searchHub", INVALID);

				if (payloadMap.containsKey("userIds")) {
					final List<Map<String, String>> userIds = (List<Map<String, String>>) payloadMap.get("userIds");
					if (!userIds.isEmpty()) {
						final Map<String, String> userDetails = userIds.get(0);
						sub = userDetails.getOrDefault("name", INVALID);
						role = userDetails.getOrDefault("type", INVALID);
						provider = userDetails.getOrDefault("provider", INVALID);
					}
				}

				final Date now = new Date();
				final String jwtToken = Jwts.builder()
						.claim(JWT_ROLE, role)
						.claim(JWT_PROVIDER, provider)
						.claim(JWT_SOURCE, source)
						.setSubject(sub)
						.setId(UUID.randomUUID().toString())
						.setIssuedAt(now)
						.setExpiration(Date.from(now.toInstant().plus(5L, ChronoUnit.MINUTES)))
						.signWith(HMAC_KEY)
						.compact();

				final SearchTokenWsDTO token = new SearchTokenWsDTO();
				token.setToken(jwtToken);
				final String jsonString = mapper.writeValueAsString(token);

				exchange.getResponseHeaders().set("Content-Type", "application/json");
				exchange.sendResponseHeaders(200, jsonString.length());
				os.write(jsonString.getBytes());
			}

		}
	}
}
