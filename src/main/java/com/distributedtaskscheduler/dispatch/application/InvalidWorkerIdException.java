package com.distributedtaskscheduler.dispatch.application;

public class InvalidWorkerIdException extends RuntimeException {

    public InvalidWorkerIdException() {
        super("Worker id must match ^[A-Za-z0-9_-]{1,64}$");
    }
}
