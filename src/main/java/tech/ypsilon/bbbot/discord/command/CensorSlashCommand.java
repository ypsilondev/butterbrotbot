package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import tech.ypsilon.bbbot.discord.listener.CensorWatcherListener;
import tech.ypsilon.bbbot.util.DiscordUtil;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.Objects;

public class CensorSlashCommand extends SlashCommand {

    @Override
    public CommandData commandData() {
        return new CommandData("censor", "Bitte versuchen Sie es erneut.")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "Benutzer, der zensiert werden soll"),
                        new OptionData(OptionType.INTEGER, "messages", "Anzahl der zu löschenden Nachrichten")
                );
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();

        if (!DiscordUtil.isAdmin(event.getMember())) {
            event.getHook().editOriginalEmbeds(EmbedUtil.createNoPermEmbed().build()).queue();
            return;
        }

        // TODO FIXME CHECK if this code actually works...

        Member member = Objects.requireNonNull(event.getOption("user")).getAsMember();
        int messageCount = (int) Objects.requireNonNull(event.getOption("messages")).getAsLong();

        if (messageCount > 0) {
            assert member != null;
            CensorWatcherListener.censoredMember.put(member.getUser(), messageCount);
        }
        Message lastMsg = CensorWatcherListener.lastMessages.get((TextChannel) event.getChannel());
        if (lastMsg != null) {
            event.getChannel().deleteMessageById(lastMsg.getId()).queue();
            CensorWatcherListener.lastMessages.put((TextChannel) event.getChannel(), null);
        }

    }
}
