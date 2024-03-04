package com.pro.speech.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class AppConfig {
    @Value("${ai.rev.auth-token}")
    public String revAuthToken;
    
}
