package tech.ypsilon.bbbot.discord.command;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import java.util.*;

public class CommandBuilder {

    private Set<String> alias = new HashSet<>();
    private GuildExecuteHandler guildHandler;
    private PrivateExecuteHandler privateHandler;
    private String description = "";

    public CommandBuilder(String... alias) {
        this.addAlias(alias);
    }

    public CommandBuilder addAlias(String... alias) {
        for (String a : alias)
            this.alias.add(a);
        return this;
    }

    public CommandBuilder setExecutor(GuildExecuteHandler geh) {
        this.guildHandler = geh;
        return this;
    }

    public CommandBuilder setExecutor(PrivateExecuteHandler peh) {
        this.privateHandler = peh;
        return this;
    }

    public CommandBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public void buildAndAdd(List<DiscordFunction> functions) {
        functions.add(this.build());
    }

    public FullStackedExecutor build() {
        FullStackedExecutor executor = new FullStackedExecutor() {
            @Override
            public String[] getAlias() {
                return alias.toArray(new String[0]);
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public void onExecute(GuildMessageReceivedEvent e, String[] args) {
                if(guildHandler != null)
                    guildHandler.onExecute(e, args);
            }

            @Override
            public void onPrivateExecute(PrivateMessageReceivedEvent e, String[] args) {
                if(privateHandler != null)
                    privateHandler.onPrivateExecute(e, args);
            }
        };
        return executor;
    }

}
