package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import tech.ypsilon.bbbot.util.DiscordUtil;

import java.util.Objects;

public class WriteAfterMeSlashCommand extends SlashCommand {
    @Override
    public CommandData commandData() {
        return new CommandData("wam", "Lässt den Bot das schreiben, was du ihm sagst")
                .addOptions(
                        new OptionData(OptionType.STRING, "text", "Der Text für den Bot", true)
                );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();
        if (DiscordUtil.isAdmin(Objects.requireNonNull(event.getMember()))) {
            String message = Objects.requireNonNull(event.getOption("text")).getAsString();
            event.getChannel().sendMessage(message).queue();
            event.getHook().editOriginal("Gesendet!").queue();
            event.getHook().deleteOriginal().queue();
        }
    }
}
