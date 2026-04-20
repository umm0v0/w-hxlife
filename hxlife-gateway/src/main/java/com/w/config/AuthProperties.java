package com.w.config;


import jdk.jfr.DataAmount;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "w.auth")
public class AuthProperties {
    private List<String> includePaths;
    private List<String> excludePaths;

}
