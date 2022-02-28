package tech.ypsilon.bbbot.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import tech.ypsilon.bbbot.ButterBrot;

@RequiredArgsConstructor
public abstract class GenericListenerController extends ListenerAdapter {
    private final @Getter
    ButterBrot parent;
}
