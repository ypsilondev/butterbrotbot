package tech.ypsilon.bbbot.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MailSubconfig {

    @JsonProperty("smtp")
    private MailSmtpSubconfig smtp;

    @Data
    @Builder
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MailSmtpSubconfig {
        private String address;
        private String host;
        private int port;
        private String username;
        private String password;
        private MailSSLSubconfig ssl;

        @Data
        @Builder
        @EqualsAndHashCode
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MailSSLSubconfig {
            private String trust;
        }
    }
}