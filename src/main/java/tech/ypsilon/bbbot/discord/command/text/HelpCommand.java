package tech.ypsilon.bbbot.discord.command.text;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import tech.ypsilon.bbbot.discord.TextCommandManager;
import tech.ypsilon.bbbot.util.EmbedUtil;

@Deprecated
public class HelpCommand extends FullStackedExecutor {
    @Override
    public String[] getAlias() {
        return new String[]{"help", "list"};
    }

    @Override
    public void onExecute(GuildMessageReceivedEvent e, String[] args) {
        answer(e.getChannel());
        e.getMessage().delete().queue();
    }

    @Override
    public void onPrivateExecute(PrivateMessageReceivedEvent e, String[] args) {
        answer(e.getChannel());
    }

    private void answer(MessageChannel channel) {
        EmbedBuilder b = EmbedUtil.createSuccessEmbed();
        b.setDescription("Folgende Commands sind implementiert und nach einem Prefix ('kit', 'kitbot', 'bb') " +
                "geschrieben, aufrufbar");
        for (Command command : TextCommandManager.getCommands()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < command.getAlias().length; i++) {
                builder.append(command.getAlias()[i]);
                if (i != command.getAlias().length - 1) builder.append(" | ");
            }
            b.addField(builder.toString(), (command.getDescription() == null ? "" : command.getDescription()), false);
        }
        channel.sendMessage(b.build()).queue();
    }

    @Override
    public String getDescription() {
        return "Listet alle implementierten Commands auf";
    }
}
