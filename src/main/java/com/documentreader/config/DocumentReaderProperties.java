package com.documentreader.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "document.reader")
public class DocumentReaderProperties {

    private final Pdf pdf = new Pdf();
    private final Ai ai = new Ai();

    public Pdf getPdf() {
        return pdf;
    }

    public Ai getAi() {
        return ai;
    }

    public static class Pdf {
        /** Max PDF download size in bytes */
        private long maxBytes = 26_214_400;
        private int maxPages = 200;
        private int fetchTimeoutSeconds = 60;
        private boolean allowInsecureHttp = false;

        public long getMaxBytes() {
            return maxBytes;
        }

        public void setMaxBytes(long maxBytes) {
            this.maxBytes = maxBytes;
        }

        public int getMaxPages() {
            return maxPages;
        }

        public void setMaxPages(int maxPages) {
            this.maxPages = maxPages;
        }

        public int getFetchTimeoutSeconds() {
            return fetchTimeoutSeconds;
        }

        public void setFetchTimeoutSeconds(int fetchTimeoutSeconds) {
            this.fetchTimeoutSeconds = fetchTimeoutSeconds;
        }

        public boolean isAllowInsecureHttp() {
            return allowInsecureHttp;
        }

        public void setAllowInsecureHttp(boolean allowInsecureHttp) {
            this.allowInsecureHttp = allowInsecureHttp;
        }
    }

    public static class Ai {
        private String apiKey = "";
        private String baseUrl = "https://api.openai.com";
        private String model = "gpt-4o-mini";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public boolean isConfigured() {
            return apiKey != null && !apiKey.isBlank();
        }
    }
}
