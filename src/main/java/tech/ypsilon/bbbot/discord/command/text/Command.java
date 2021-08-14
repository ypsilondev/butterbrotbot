package tech.ypsilon.bbbot.discord.command.text;

public interface Command extends DiscordFunction {

    String[] getAlias();
    String getDescription();

}
