package tech.ypsilon.bbbot.config;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSubconfig {

    private long productiveRoleId;

    private List<NameEmojiRole> yearRoles;
    private List<NameEmojiRole> degreeRoles;

    private Map<String, CourseCategorySubconfig> courseCategories;

    @Data
    @Builder
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NameEmojiRole {
        private String unicodeEmoji;
        private String name;
        private long discordRoleId;
    }

    @Data
    @Builder
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseCategorySubconfig {
        private String unicodeEmoji;
        private String name;

        private Map<String, NameEmojiRole> courses;
    }
}
