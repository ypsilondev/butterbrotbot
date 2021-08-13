package tech.ypsilon.bbbot.console;

public abstract class ConsoleCommand {

    public abstract String[] getAlias();

    public abstract String getDescription();

    public abstract void onExecute(String[] args);

}
