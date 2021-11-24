package tech.ypsilon.bbbot.discord.listener;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import tech.ypsilon.bbbot.ButterBrot;

@RequiredArgsConstructor
public abstract class ButterbrotListener extends ListenerAdapter {
    private final @Getter ButterBrot parent;
}
