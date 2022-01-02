package tech.ypsilon.bbbot.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class DiscordSubconfig {

    @JsonProperty("prefix")
    private List<String> prefixList;

    @JsonProperty("token")
    private String discordBotToken;

    @JsonProperty("guild")
    private Long homeGuildId;

    @JsonProperty("roles")
    private Map<String, Long> discordRoles;

    @JsonProperty("toolsurl")
    private String toolsURL;

    @JsonProperty("aliases")
    private String aliasesURL;

    @JsonProperty("studiengaenge")
    private CoursesSubconfig courseSelectionConfig;

    @JsonProperty("logchannel")
    private Long logChannelId;

    @JsonProperty("studyGroupCategory")
    private Long studyGroupCategory;

    @JsonProperty("studyJoinChannel")
    private Long studyJoinChannel;

    /* --- */

    @Data
    @Builder
    @EqualsAndHashCode
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CoursesSubconfig {
        private Long channel;
    }
}
