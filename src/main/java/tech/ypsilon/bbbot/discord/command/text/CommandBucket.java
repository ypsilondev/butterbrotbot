package tech.ypsilon.bbbot.discord.command.text;

import java.util.List;

@Deprecated
public interface CommandBucket extends DiscordFunction {

    void register(List<DiscordFunction> functions);

}
