package tech.ypsilon.bbbot.discord.command;

public abstract class Command {

    public abstract String[] getAlias();
    public abstract String getDescription();

}
