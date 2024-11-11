package uk.gov.companieshouse.chsmonitorapi.controller;

import static uk.gov.companieshouse.chsmonitorapi.ChsMonitorApiApplication.APPLICATION_NAME_SPACE;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.model.Subscription;
import uk.gov.companieshouse.chsmonitorapi.service.SubscriptionService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
@RequestMapping("/following")
public class ChsMonitorApiController {

    private static final Logger logger = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);
    private final SubscriptionService subscriptionService;

    @Autowired
    public ChsMonitorApiController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public ResponseEntity<List<Subscription>> getSubscriptions(HttpServletRequest request,
            @RequestParam @NonNull String companyNumber, @RequestParam @NonNull int startIndex,
            @RequestParam @NonNull int itemsPerPage) {
        try {
            List<Subscription> subscriptions = subscriptionService.getSubscriptions(
                    request.getSession().getId(), companyNumber, startIndex, itemsPerPage);
            return ResponseEntity.ok(subscriptions);
        } catch (ArrayIndexOutOfBoundsException exception) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
        } catch (ServiceException exception) {
            // TODO: handle service exception
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{companyNumber}")
    public ResponseEntity<Subscription> getSubscription(HttpServletRequest request,
            @PathVariable("companyNumber") @NonNull String companyNumber) {
        try {
            Subscription subscription = subscriptionService.getSubscription(
                    request.getSession().getId(), companyNumber);
            return ResponseEntity.ok(subscription);
        } catch (ServiceException exception) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{companyNumber}")
    public ResponseEntity<HttpStatus> createSubscription(HttpServletRequest request,
            @PathVariable @NonNull String companyNumber) {
        try {
            subscriptionService.createSubscription(request.getSession().getId(), companyNumber);
            return ResponseEntity.ok().build();
        } catch (ServiceException exception) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{companyNumber}")
    public ResponseEntity<HttpStatus> deleteSubscription(HttpServletRequest request,
            @PathVariable @NonNull String companyNumber) {
        try {
            subscriptionService.deleteSubscription(request.getSession().getId(), companyNumber);
            return ResponseEntity.ok().build();
        } catch (ServiceException exception) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
