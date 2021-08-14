package tech.ypsilon.bbbot.discord.command.text;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tech.ypsilon.bbbot.discord.ServiceManager;
import tech.ypsilon.bbbot.discord.services.AliasService;
import tech.ypsilon.bbbot.discord.services.ToolUpdaterService;
import tech.ypsilon.bbbot.util.DiscordUtil;
import tech.ypsilon.bbbot.util.EmbedUtil;

public class ReloadCommand implements GuildExecuteHandler {
    @Override
    public String[] getAlias() {
        return new String[]{"reload"};
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        e.getMessage().delete().queue();
        e.getAuthor().openPrivateChannel().queue(channel -> handle(e.getMember(), channel));
    }

    private void handle(Member member, MessageChannel channel) {
        if (!DiscordUtil.isAdmin(member)) {
            channel.sendMessage(EmbedUtil.createNoPermEmbed().build()).queue();
            return;
        }
        ServiceManager.getInstance().findNotifierService(AliasService.class).execute(null);
        ServiceManager.getInstance().findNotifierService(ToolUpdaterService.class).execute(null);
        channel.sendMessage(EmbedUtil.createSuccessEmbed().setDescription("Die Aliase und tools wurden reloaded!").build()).queue();
    }
}
