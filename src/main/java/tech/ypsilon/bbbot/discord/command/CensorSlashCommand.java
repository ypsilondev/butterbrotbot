package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Nullable;
import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.discord.listener.CensorWatcherListener;
import tech.ypsilon.bbbot.util.DiscordUtil;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.Objects;

/**
 * Command to censor a member's messages
 *
 * @implNote ported by Shirkanesi
 */
public class CensorSlashCommand extends SlashCommand {

    public CensorSlashCommand(ButterBrot parent) {
        super(parent);
    }

    @Override
    public CommandData commandData() {
        return new CommandData("censor", "Bitte versuchen Sie es erneut.")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "Benutzer, der zensiert werden soll", true),
                        new OptionData(OptionType.INTEGER, "messages", "Anzahl der zu löschenden Nachrichten", true)
                );
    }

    @Override
    public @Nullable String getHelpDescription() {
        return "/censor <member> <number> löscht die letzte Nachricht von <member> und die nächsten <number> vielen.";
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
