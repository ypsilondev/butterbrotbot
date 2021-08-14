package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import tech.ypsilon.bbbot.discord.ServiceManager;
import tech.ypsilon.bbbot.discord.services.AliasService;
import tech.ypsilon.bbbot.discord.services.ToolUpdaterService;
import tech.ypsilon.bbbot.util.DiscordUtil;
import tech.ypsilon.bbbot.util.EmbedUtil;

import java.util.Objects;

public class ReloadSlashCommand extends SlashCommand {
    @Override
    public CommandData commandData() {
        return new CommandData("reload", "Läd alle Aliase und Tools neu");
    }

    @Override
    public void execute(SlashCommandEvent event) {
        event.deferReply(true).queue();
        if (!DiscordUtil.isAdmin(Objects.requireNonNull(event.getMember()))) {
            event.getHook().editOriginalEmbeds(EmbedUtil.createNoPermEmbed().build()).queue();
            return;
        }
        ServiceManager.getInstance().findNotifierService(AliasService.class).execute(null);
        ServiceManager.getInstance().findNotifierService(ToolUpdaterService.class).execute(null);
        event.getHook().editOriginalEmbeds(EmbedUtil.createSuccessEmbed().setDescription("Die Aliase und tools wurden reloaded!").build()).queue();
    }
}
