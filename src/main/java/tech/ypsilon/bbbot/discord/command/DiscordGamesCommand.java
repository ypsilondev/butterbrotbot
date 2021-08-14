package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class DiscordGamesCommand extends SlashCommand {

    @Override
    public CommandData commandData() {
        return new CommandData("games", "Spiele diverse Spiele direkt in Discord")
                .addOption(OptionType.CHANNEL, "channel", "Der Sprachkanal für den die Einladung erstellt wird", true)
                .addOption(OptionType.BOOLEAN, "public", "Erstelle eine öfentliche Einladung", false);
    }

    @Override
    public void execute(SlashCommandEvent event) {
        VoiceChannel targetChannel;
        boolean publicInvite = false;

        try {
            targetChannel = (VoiceChannel) event.getOption("channel").getAsGuildChannel();
            if (event.getOption("public") != null) {
                publicInvite = event.getOption("public").getAsBoolean();
            }
        } catch (ClassCastException exception) {
            throw new CommandFailedException("Du musst einen Sprachkanal angeben!");
        } catch (NullPointerException exception) {
            throw new CommandFailedException();
        }

        // TODO: Placeholder until JDA finally implements the application invite feature!!!!1
    }
}
