package uk.gov.companieshouse.chsmonitorapi.controller;

import static uk.gov.companieshouse.chsmonitorapi.interceptor.AuthenticationInterceptor.ERIC_IDENTITY;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.model.InputSubscription;
import uk.gov.companieshouse.chsmonitorapi.model.SubscriptionDocument;
import uk.gov.companieshouse.chsmonitorapi.service.SubscriptionService;
import uk.gov.companieshouse.logging.Logger;

@RestController
@RequestMapping("/following")
@EnableSpringDataWebSupport
public class ChsMonitorApiController {

    private final Logger logger;
    private final SubscriptionService subscriptionService;

    @Autowired
    public ChsMonitorApiController(SubscriptionService subscriptionService, Logger logger) {
        this.subscriptionService = subscriptionService;
        this.logger = logger;
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<SubscriptionDocument>>> getSubscriptions(
            HttpServletRequest request, @RequestParam @NonNull int startIndex,
            @RequestParam @NonNull int itemsPerPage,
            PagedResourcesAssembler<SubscriptionDocument> assembler) {
        try {
            Pageable pageable = PageRequest.of(startIndex / itemsPerPage, itemsPerPage);
            Page<SubscriptionDocument> subscriptions = subscriptionService.getSubscriptions(
                    request.getHeader(ERIC_IDENTITY), pageable);
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Page-Number", String.valueOf(subscriptions.getNumber()));
            headers.add("X-Page-Size", String.valueOf(subscriptions.getSize()));
            return ResponseEntity.ok().headers(headers).body(assembler.toModel(subscriptions));
        } catch (ArrayIndexOutOfBoundsException exception) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
        } catch (ServiceException exception) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{companyNumber}")
    public ResponseEntity<SubscriptionDocument> getSubscription(HttpServletRequest request,
            @PathVariable String companyNumber) {
        try {
            SubscriptionDocument subscription = subscriptionService.getSubscription(
                    request.getHeader(ERIC_IDENTITY), companyNumber);
            return ResponseEntity.ok(subscription);
        } catch (ServiceException exception) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<HttpStatus> createSubscription(HttpServletRequest request,
            @Valid @RequestBody InputSubscription inputSubscription) {
        try {
            subscriptionService.createSubscription(request.getHeader(ERIC_IDENTITY),
                    inputSubscription.getCompanyNumber());
            return ResponseEntity.ok().build();
        } catch (ServiceException exception) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteSubscription(HttpServletRequest request,
            @Valid @RequestBody InputSubscription inputSubscription) {
        try {
            subscriptionService.deleteSubscription(request.getHeader(ERIC_IDENTITY),
                    inputSubscription.getCompanyNumber());
            return ResponseEntity.ok().build();
        } catch (ServiceException exception) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
