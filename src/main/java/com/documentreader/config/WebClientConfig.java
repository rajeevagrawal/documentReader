package com.documentreader.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient pdfWebClient(DocumentReaderProperties properties) {
        int max = (int) Math.min(Integer.MAX_VALUE, properties.getPdf().getMaxBytes());
        HttpClient httpClient =
                HttpClient.create().responseTimeout(Duration.ofSeconds(properties.getPdf().getFetchTimeoutSeconds()));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> {
                    ClientCodecConfigurer.DefaultCodecs defaults = configurer.defaultCodecs();
                    defaults.maxInMemorySize(max);
                })
                .build();
    }
}
