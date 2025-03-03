package com.tcc.edlaine.crosscutting.middlewares;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);

        if(response.getStatusCode().isError()) {
            InputStreamReader reader = new InputStreamReader(response.getBody(), StandardCharsets.UTF_8);
            String errorBody = new BufferedReader(reader).lines().collect(Collectors.joining("\n"));
            log.info("[restClient response]: {} - {} {} with error: {}", response.getStatusCode(), request.getMethod(), request.getURI(), errorBody);
        } else {
            log.info("[restClient]: {} - {} {}", response.getStatusCode(), request.getMethod(), request.getURI());
        }

        return response;
    }

}
