package com.diacenco.scheduler.dispatch.api;

import com.diacenco.scheduler.dispatch.application.WorkerDispatchService;
import com.diacenco.scheduler.ratelimit.domain.DispatchPermit;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dispatch")
public class DispatchController {

    private final WorkerDispatchService workerDispatchService;

    public DispatchController(WorkerDispatchService workerDispatchService) {
        this.workerDispatchService = workerDispatchService;
    }

    @PostMapping("/permit")
    public DispatchPermitResponse requestPermit() {
        DispatchPermit permit = workerDispatchService.requestDispatchPermit();
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
