package de.markusdope.stats.data.dto;

import lombok.Data;

@Data
public class LoginToken {
    private final String token;
    private final String cookie;

    @Data
    public static class LoginTokenResponse {
        private String batchcomplete;
        private Query query;

        @Data
        public static class Query {
            private Tokens tokens;

            @Data
            public static class Tokens {
                private String logintoken;
            }
        }

    }
}
