package com.winten.greenlight.sample.greenlight;

import lombok.Data;

@Data
public class QueueCheckResponse {
    private Integer actionId;
    private String customerId;
    private String destinationUrl;
    private Long timestamp;
    private WaitStatus waitStatus;
    private String jwtToken;
    private Boolean verified;
}