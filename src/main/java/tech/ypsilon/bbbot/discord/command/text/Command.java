package tech.ypsilon.bbbot.discord.command.text;

@Deprecated
public interface Command extends DiscordFunction {

    String[] getAlias();
    String getDescription();

}
