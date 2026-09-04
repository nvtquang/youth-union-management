package com.hcmcyu.gateway.service;

import com.hcmcyu.gateway.dto.HealthResponse;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    public HealthResponse getHealth() {
        return new HealthResponse("api-gateway", "UP");
    }
}

