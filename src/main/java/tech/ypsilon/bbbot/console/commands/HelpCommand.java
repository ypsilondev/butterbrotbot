package tech.ypsilon.bbbot.console.commands;

import tech.ypsilon.bbbot.ButterBrot;
import tech.ypsilon.bbbot.console.ConsoleCommand;
import tech.ypsilon.bbbot.console.ConsoleController;

import static tech.ypsilon.bbbot.ButterBrot.LOGGER;

public class HelpCommand extends ConsoleCommand {

    public HelpCommand(ButterBrot parent) {
        super(parent);
    }

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
        for(ConsoleCommand command : ConsoleController.getCommands()){
            LOGGER.info(command.getAlias()[0] + " - " + command.getDescription());
        }
    }
}
