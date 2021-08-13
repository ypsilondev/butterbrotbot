package tech.ypsilon.bbbot.console.commands;

import tech.ypsilon.bbbot.console.ConsoleCommand;
import tech.ypsilon.bbbot.console.ConsoleManager;

import static tech.ypsilon.bbbot.ButterBrot.LOGGER;

public class HelpCommand extends ConsoleCommand {

    @Override
    public String[] getAlias() {
        return new String[]{"help", "?"};
    }

    @Override
    public String getDescription() {
        return "Returns a list of all commands";
    }

    @Override
    public void onExecute(String[] args) {
        LOGGER.info("List of all commands: ");
        for (ConsoleCommand command : ConsoleManager.getCommands()) {
            LOGGER.info(command.getAlias()[0] + " - " + command.getDescription());
        }
    }
}
