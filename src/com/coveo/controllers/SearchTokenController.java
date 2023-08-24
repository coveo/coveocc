package com.coveo.controllers;

import com.coveo.SearchTokenWsDTO;
import com.coveo.constants.CoveoccConstants;
import com.coveo.facades.SearchTokenFacade;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping(value="/{baseSiteId}/coveo")
public class SearchTokenController {

    private static String ANONYMOUS_USER_ID="anonymous";

    @Resource(name = "searchTokenFacade")
    private SearchTokenFacade searchTokenFacade;


    @RequestMapping(value="token/{searchHub}", method= RequestMethod.GET)
    @CacheControl(directive = CacheControlDirective.PRIVATE,maxAge = CoveoccConstants.SEARCH_TOKEN_MAX_AGE_SECONDS)
    @ResponseBody
    @ApiOperation(nickname="getSearchToken", value="Get search token for anonymous user and specific search hub",
            notes="Return a token that can be used with search api ")

    @ApiBaseSiteIdParam
    public ResponseEntity getSearchToken(@ApiParam(value="baseSiteId", required=true) @PathVariable final String baseSiteId,
                                           @ApiParam(value="searchHub", required=true) @PathVariable final String searchHub)
    {
        return searchTokenFacade.getSearchToken(baseSiteId, ANONYMOUS_USER_ID, searchHub, TimeUnit.SECONDS.toMillis(CoveoccConstants.SEARCH_TOKEN_MAX_AGE_SECONDS));
    }
}
