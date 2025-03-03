package com.tcc.edlaine.crosscutting.properties.restClient;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "rest-client")
public class RestClientPropertiesMap {

    private Map<String, RestClientProperties> configurations = new HashMap<>();

}
