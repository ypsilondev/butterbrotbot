package tech.ypsilon.bbbot.discord.command;

import java.util.ArrayList;
import java.util.List;

public interface CommandBucket extends DiscordFunction {

    void register(List<DiscordFunction> functions);

}
