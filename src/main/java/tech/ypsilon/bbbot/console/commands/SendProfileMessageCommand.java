package tech.ypsilon.bbbot.console.commands;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.console.ConsoleCommand;
import tech.ypsilon.bbbot.discord.SlashCommandController;
import tech.ypsilon.bbbot.discord.command.ProfileCommand;
import tech.ypsilon.bbbot.util.EmbedUtil;

@Slf4j
public class SendProfileMessageCommand extends ConsoleCommand {

    private static final Message WELCOME_PROFILE_MESSAGE = new MessageBuilder()
            .setEmbeds(
                    EmbedUtil.createInfoEmbed()
                            .setTitle("🇩🇪 Wähle dein Profil")
                            .setDescription("Herzlich willkommen auf dem Allgemeinen Discord Server für Studierende " +
                                    "des KIT. Um alle Kanäle für dein Studium zu sehen, wähle hier dein Profil aus.")
                            .setFooter("")
                            .build(),
                    EmbedUtil.createInfoEmbed()
                            .setTitle("🇬🇧🇺🇸 Select Your Profile")
                            .setDescription("Welcome to the general Discord server for KIT students. In order to "
                                    + "view all channels for your course, select your profile here.")
                            .setFooter("")
                            .build()
            ).setActionRows(ActionRow.of(
                    Button.success(
                            SlashCommandController.BUTTON_PREFIX
                                    + SlashCommandController.INTERACTION_ID_DELIMITER + "profile"
                                    + SlashCommandController.INTERACTION_ID_DELIMITER + ProfileCommand.BTN_DATA_CREATE_MENU
                                    + SlashCommandController.INTERACTION_ID_DELIMITER + "0",
                            "\uD83D\uDCAA Dein Studiengang" // muscle Emoji
                    ),
                    Button.primary(
                            SlashCommandController.BUTTON_PREFIX
                                    + SlashCommandController.INTERACTION_ID_DELIMITER + "profile"
                                    + SlashCommandController.INTERACTION_ID_DELIMITER + ProfileCommand.BTN_DATA_CREATE_DEGREE
                                    + SlashCommandController.INTERACTION_ID_DELIMITER + "0",
                            "\uD83C\uDF93 Angestrebter Abschluss" // muscle Emoji
                    ),
                    Button.primary(
                            SlashCommandController.BUTTON_PREFIX
                                    + SlashCommandController.INTERACTION_ID_DELIMITER + "profile"
                                    + SlashCommandController.INTERACTION_ID_DELIMITER + ProfileCommand.BTN_DATA_CREATE_YEAR
                                    + SlashCommandController.INTERACTION_ID_DELIMITER + "0",
                            "\uD83D\uDCC5 Startsemester" // muscle Emoji
                    )
            )).build();

    private static final Message PRODUCTIVE_MESSAGE = new MessageBuilder()
            .setEmbeds(
                    EmbedUtil.createInfoEmbed()
                            .setTitle("🇩🇪 Ich bin Produktiv")
                            .setDescription("Falls du nicht von Freizeitaktivitäten abgelenkt werden möchtest, "
                                    + "kannst du diese Kanäle hiermit ausblenden.")
                            .setFooter("")
                            .build(),
                    EmbedUtil.createInfoEmbed()
                            .setTitle("🇬🇧🇺🇸 I am Productive")
                            .setDescription("If you don't want to be distracted by free time activities, "
                                    + "you can chose to hide those channels here.")
                            .setFooter("")
                            .build()
            ).setActionRows(ActionRow.of(
                    Button.danger(
                            SlashCommandController.BUTTON_PREFIX
                                    + SlashCommandController.INTERACTION_ID_DELIMITER + "profile"
                                    + SlashCommandController.INTERACTION_ID_DELIMITER + ProfileCommand.BTN_DATA_PRODUCTIVE
                                    + SlashCommandController.INTERACTION_ID_DELIMITER + "0",
                            "\uD83E\uDDD1\u200D\uD83C\uDF93 Produktiv Ein/Aus" // muscle Emoji
                    )
            )).build();

    public SendProfileMessageCommand(ButterBrot parent) {
        super(parent);
    }

    @Override
    public String[] getAlias() {
        return new String[]{"sendProfileMessage"};
    }

    @Override
    public String getDescription() {
        return "Usage: `sendProfileMessage <channelId>`. Send welcome message containing the profile edit button to the given channel";
    }

    @Override
    public void onExecute(String[] args) {
        if (args.length != 2 || args[1].isBlank()) {
            log.error("Invalid number of arguments, 1 required!");
            return;
        }

        TextChannel textChannel = getParent().getDiscordController().getHome().getTextChannelById(args[1]);

        if (textChannel == null) {
            log.error("Text channel {} not found!", args[1]);
            return;
        }

        textChannel.sendMessage(PRODUCTIVE_MESSAGE).queue(
                message -> log.info("Success 1/2! (probably...)"),
                throwable -> log.error("1/2: Message could not be sent!", throwable)
        );

        textChannel.sendMessage(WELCOME_PROFILE_MESSAGE).queue(
                message -> log.info("Success 2/2! (probably...)"),
                throwable -> log.error("2/2: Message could not be sent!", throwable)
        );

    }
}
