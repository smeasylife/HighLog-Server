package goatHeaven.highLog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Component
public class CorsConfig {

    @Value("${cors.allow.origins:*}")
    private String allowedOriginPatterns;

    @Value("${cors.allow.headers:*}")
    private String allowedHeaders;

    @Value("${cors.expose.headers:*}")
    private String exposedHeaders;

    @Value("${cors.allow.methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;

    @Value("${cors.allow.credentials:true}")
    private boolean allowCredentials;

    @Bean
    @Primary
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration config = new CorsConfiguration();

        List<String> originList = Arrays.asList(allowedOriginPatterns.split(","));
        List<String> headerList = Arrays.asList(allowedHeaders.split(","));
        List<String> exposedHeaderList = Arrays.asList(exposedHeaders.split(","));
        List<String> methodList = Arrays.asList(allowedMethods.split(","));


        if (allowedOriginPatterns.equals("*")) {
            config.setAllowedOriginPatterns(List.of("*"));
            config.setAllowCredentials(false);
        } else {
            config.setAllowedOrigins(originList);
            config.setAllowCredentials(allowCredentials);
        }
        config.setAllowedHeaders(headerList);
        config.setExposedHeaders(exposedHeaderList);
        config.setAllowedMethods(methodList);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
