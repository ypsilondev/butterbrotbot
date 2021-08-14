package tech.ypsilon.bbbot.discord.command;

public class CommandFailedException extends RuntimeException {

    public CommandFailedException(String message) {
        super(message);
    }

}
