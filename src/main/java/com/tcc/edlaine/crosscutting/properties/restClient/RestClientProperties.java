package com.tcc.edlaine.crosscutting.properties.restClient;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestClientProperties {

    private String host;
    private int readTimeout;
    private int connectTimeout;

}
