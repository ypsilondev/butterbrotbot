package tech.ypsilon.bbbot.config;

import lombok.*;

@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MongoSubconfig {
    private String host;
    private int port;
    private String username;
    private String authDatabase;
    private String password;
}
