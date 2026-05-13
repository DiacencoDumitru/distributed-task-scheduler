package com.distributedtaskscheduler.dispatch.api;

import com.distributedtaskscheduler.dispatch.application.DispatchScope;
import com.distributedtaskscheduler.dispatch.application.WorkerDispatchService;
import com.distributedtaskscheduler.ratelimit.domain.DispatchPermit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dispatch")
@RestController
@RequestMapping("/api/v1/dispatch")
public class DispatchController {

    private final WorkerDispatchService workerDispatchService;

    public DispatchController(WorkerDispatchService workerDispatchService) {
        this.workerDispatchService = workerDispatchService;
    }

    @Operation(summary = "Request a dispatch permit")
    @ApiResponse(
            responseCode = "200",
            description = "Permit granted",
            content = @Content(schema = @Schema(implementation = DispatchPermitResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid dispatch scope header")
    @ApiResponse(responseCode = "429", description = "Dispatch rate limit exceeded")
    @PostMapping("/permit")
    public DispatchPermitResponse requestPermit(
            @Parameter(name = "X-Dispatch-Scope", in = ParameterIn.HEADER, required = false)
            @RequestHeader(value = "X-Dispatch-Scope", required = false) String dispatchScope
    ) {
        String limiterKey = DispatchScope.toLimiterKey(dispatchScope);
        DispatchPermit permit = workerDispatchService.requestDispatchPermit(limiterKey);
        if (!permit.allowed()) {
            throw new DispatchRateLimitExceededException(permit);
        }

        return DispatchPermitResponse.from(permit);
    }

    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    private static class DispatchRateLimitExceededException extends RuntimeException {
        private DispatchRateLimitExceededException(DispatchPermit permit) {
            super("Dispatch rate limit exceeded");
        }
    }
}
