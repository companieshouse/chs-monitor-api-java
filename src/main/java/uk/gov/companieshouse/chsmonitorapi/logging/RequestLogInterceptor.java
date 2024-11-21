package uk.gov.companieshouse.chsmonitorapi.logging;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.RequestLogger;

public class RequestLogInterceptor implements HandlerInterceptor, RequestLogger {

    private final Logger logger;

    @Autowired
    public RequestLogInterceptor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response, @Nonnull Object handler) throws Exception {
        logStartRequestProcessing(request, logger);
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(@Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response, @Nonnull Object handler,
            ModelAndView modelAndView) throws Exception {
        logEndRequestProcessing(request, response, logger);
        // Without this debug call the endrequestprocessing isn't called until a new request
        // comes in. god knows why
        logger.trace("Calling super postHandle");
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
}
