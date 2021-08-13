package tech.ypsilon.bbbot.discord.command;

public interface Command extends DiscordFunction {

    String[] getAlias();

    String getDescription();

}
