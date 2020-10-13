package tech.ypsilon.bbbot.discord.command;

import java.util.List;

public interface CommandBucket extends DiscordFunction {

    List<DiscordFunction> register();

}
