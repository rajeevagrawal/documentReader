package com.documentreader.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "document.reader")
public class DocumentReaderProperties {

    private final Pdf pdf = new Pdf();
    private final Ai ai = new Ai();
    private final Storage storage = new Storage();

    public Pdf getPdf() {
        return pdf;
    }

    public Ai getAi() {
        return ai;
    }

    public Storage getStorage() {
        return storage;
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
        /**
         * Full chat-completions endpoint URL (for example:
         * {@code https://api.openai.com/v1/chat/completions}).
         */
        private String chatCompletionsUrl = "https://api.openai.com/v1/chat/completions";
        private String model = "gpt-4o-mini";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getChatCompletionsUrl() {
            return chatCompletionsUrl;
        }

        public void setChatCompletionsUrl(String chatCompletionsUrl) {
            this.chatCompletionsUrl = chatCompletionsUrl;
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

    public static class Storage {
        /**
         * When true, the service writes retrieved PDFs and generated .docx bytes to the configured
         * directories on the local filesystem.
         */
        private boolean enabled = true;

        private String pdfDir = "src/main/resources/local-storage/pdfs";
        private String wordDir = "src/main/resources/local-storage/output-docx";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPdfDir() {
            return pdfDir;
        }

        public void setPdfDir(String pdfDir) {
            this.pdfDir = pdfDir;
        }

        public String getWordDir() {
            return wordDir;
        }

        public void setWordDir(String wordDir) {
            this.wordDir = wordDir;
        }
    }
}
