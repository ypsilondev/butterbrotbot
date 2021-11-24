package tech.ypsilon.bbbot.console.commands;

import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.console.ConsoleCommand;

import static tech.ypsilon.bbbot.ButterBrot.LOGGER;

public class StopCommand extends ConsoleCommand {

    @Override
    public String[] getAlias() {
        return new String[]{"stop"};
    }

    @Override
    public String getDescription() {
        return "Stop the bot";
    }

    @Override
    public void onExecute(String[] args) {
        LOGGER.info("Stopping bot...");
        ButterBrot.stopBot(true);
    }

}
