package uk.gov.companieshouse.chsmonitorapi.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    public static final String ERIC_IDENTITY = "ERIC-Identity";
    public static final String ERIC_IDENTITY_TYPE = "ERIC-Identity-Type";
    private final Logger logger;


    public AuthenticationInterceptor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        String identityType = request.getHeader(ERIC_IDENTITY_TYPE);

        if (StringUtils.isEmpty(request.getHeader(ERIC_IDENTITY)) || (
                StringUtils.isEmpty(identityType) || isInvalidIdentityType(identityType))) {
            logger.errorRequest(request, "User not authenticated",
                    new DataMap.Builder().build().getLogMap());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

//        if (!isKeyAuthorised(request, identityType)) {
//            logger.errorRequest(request,
//                    "Supplied key does not have sufficient privilege for the action",
//                    new DataMap.Builder().build().getLogMap());
//            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//            return false;
//        }

        logger.debugRequest(request, "User authenticated", new DataMap.Builder().build().getLogMap());
        return true;
    }

//    private boolean isKeyAuthorised(HttpServletRequest request, String ericIdentityType) {
//        String[] privileges = authenticationHelper.getApiKeyPrivileges(request);
//
//        return HttpMethod.GET.matches(request.getMethod()) || (
//                "key".equalsIgnoreCase(ericIdentityType) && ArrayUtils.contains(privileges,
//                        "internal-app"));
//    }

    private boolean isInvalidIdentityType(String identityType) {
        return !("key".equalsIgnoreCase(identityType) || "oauth2".equalsIgnoreCase(identityType));
    }
}