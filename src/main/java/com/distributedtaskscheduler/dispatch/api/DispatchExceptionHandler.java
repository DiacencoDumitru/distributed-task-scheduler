package com.distributedtaskscheduler.dispatch.api;

import com.distributedtaskscheduler.dispatch.application.InvalidWorkerIdException;
import com.distributedtaskscheduler.ratelimit.domain.DispatchPermit;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DispatchExceptionHandler {

    @ExceptionHandler(DispatchRateLimitExceededException.class)
    public ResponseEntity<ProblemDetail> handleDispatchRateLimit(DispatchRateLimitExceededException ex) {
        DispatchPermit permit = ex.permit();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS,
                "Dispatch rate limit exceeded"
        );
        problemDetail.setTitle("Too many dispatch requests");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, Long.toString(permit.windowSeconds()))
                .body(problemDetail);
    }

    @ExceptionHandler(InvalidWorkerIdException.class)
    public ResponseEntity<ProblemDetail> handleInvalidWorkerId(InvalidWorkerIdException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Invalid worker id");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
}
