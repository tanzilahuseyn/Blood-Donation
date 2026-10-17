package ee.taltech.ite4120.blood.api;

import ee.taltech.ite4120.blood.operatorregistry.OperatorRegistryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * The one error this component adds to the shared ApiErrorHandler: the business
 * register being down is an upstream failure, not the caller's fault — 502, as
 * application/problem+json like every other error.
 */
@RestControllerAdvice
public class BloodErrorHandler {

    @ExceptionHandler(OperatorRegistryException.class)
    public ProblemDetail registerFailure(OperatorRegistryException e) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_GATEWAY);
        pd.setTitle("Business register unavailable");
        pd.setDetail(e.getMessage());
        return pd;
    }
}
