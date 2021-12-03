package tech.ypsilon.bbbot.console;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tech.ypsilon.bbbot.ButterBrot;

@RequiredArgsConstructor
public abstract class ConsoleCommand {

    private final @Getter ButterBrot parent;

    public abstract String[] getAlias();
    public abstract String getDescription();
    public abstract void onExecute(String[] args);

}
