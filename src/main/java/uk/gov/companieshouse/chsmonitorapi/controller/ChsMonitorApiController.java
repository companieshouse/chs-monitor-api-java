package uk.gov.companieshouse.chsmonitorapi.controller;

import static uk.gov.companieshouse.chsmonitorapi.ChsMonitorApiApplication.APPLICATION_NAME_SPACE;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/{companyNumber}")
    public ResponseEntity<List<Subscription>> getSubscriptions(HttpServletRequest request,
            @PathVariable @NonNull String companyNumber, @PathVariable @NonNull int startIndex,
            @PathVariable @NonNull int itemsPerPage) {
        try {
            List<Subscription> subscriptions = subscriptionService.getSubscriptions(companyNumber,
                    startIndex, itemsPerPage);
            return ResponseEntity.ok(subscriptions);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
        } catch (ServiceException e) {
            // TODO: handle service exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
