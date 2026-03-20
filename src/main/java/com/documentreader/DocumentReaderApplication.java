package com.documentreader;

import com.documentreader.config.DocumentReaderProperties;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DocumentReaderProperties.class)
public class DocumentReaderApplication {

    public static void main(String[] args) {
        loadDotenvIntoSystemProperties();
        SpringApplication.run(DocumentReaderApplication.class, args);
    }

    /**
     * Loads {@code .env} from the working directory when present. Does not override variables already
     * set in the process environment (so CI and shell exports win over the file).
     */
    private static void loadDotenvIntoSystemProperties() {
        Dotenv dotenv =
                Dotenv.configure()
                        .directory("./")
                        .ignoreIfMalformed()
                        .ignoreIfMissing()
                        .load();
        dotenv.entries()
                .forEach(
                        e -> {
                            if (System.getenv(e.getKey()) != null
                                    || System.getProperty(e.getKey()) != null) {
                                return;
                            }
                            System.setProperty(e.getKey(), e.getValue());
                        });
    }
}
