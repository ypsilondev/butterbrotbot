package tech.ypsilon.bbbot.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ButterbrotConfig extends DefaultConfigFactory {
    private MongoSubconfig mongo;
    private DiscordSubconfig discord;
    private CommandsSubconfig commands;
    private MailSubconfig mail;
    private ProfileSubconfig profile;

    @SuppressWarnings("unused")
    public static DefaultConfigFactory createDefault() {
        return ButterbrotConfig.builder()
                .mongo(MongoSubconfig.builder()
                        .host("example.com")
                        .port(27017)
                        .username("username")
                        .authDatabase("authDatabase")
                        .password("password")
                        .butterbrotDatabase("ButterBrot")
                        .build())
                .discord(DiscordSubconfig.builder()
                        .prefixList(List.of("kitbot", "bb", "kit"))
                        .discordBotToken("token_String")
                        .homeGuildId(-1L)
                        .discordRoles(Map.of("admin", -1L,
                                "student", -1L))
                        .toolsURL("https://example.com/tools.txt")
                        .aliasesURL("https://example.com/aliases.txt")
                        .courseSelectionConfig(DiscordSubconfig.CoursesSubconfig.builder()
                                .channel(-1L)
                                .build())
                        .logChannelId(-1L)
                        .studyGroupCategory(-1L)
                        .studyJoinChannel(-1L)
                        .build())
                .commands(CommandsSubconfig.builder()
                        .birthday(CommandsSubconfig.BDaySubconfig.builder()
                                .notifyNoBirthdays(false)
                                .build())
                        .birthdayChannel(-1L)
                        .build())
                .mail(MailSubconfig.builder()
                        .smtp(MailSubconfig.MailSmtpSubconfig.builder()
                                .address("noreply@example.com")
                                .host("smtp.example.com")
                                .port(456)
                                .username("username")
                                .password("password")
                                .ssl(MailSubconfig.MailSmtpSubconfig.MailSSLSubconfig.builder()
                                        .trust("smtp.example.com")
                                        .build())
                                .build())
                        .build())
                .profile(ProfileSubconfig.builder()
                        .courseCategories(Map.of("category1", ProfileSubconfig.CourseCategorySubconfig.builder()
                                        .name("Category 1")
                                        .unicodeEmoji("\uD83D\uDDFF")
                                        .courses(Map.of("course1", ProfileSubconfig.NameEmojiRole.builder()
                                                        .name("Course 1")
                                                        .unicodeEmoji("\uD83D\uDDFF")
                                                        .discordRoleId(-1)
                                                .build()))
                                .build()))
                        .build())
                .build();
    }
}
