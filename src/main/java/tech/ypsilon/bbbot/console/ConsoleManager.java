package tech.ypsilon.bbbot.console;

import tech.ypsilon.bbbot.console.commands.StopCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static tech.ypsilon.bbbot.ButterBrot.LOGGER;

public class ConsoleManager implements Runnable{

    private static ConsoleManager instance;

    private final List<ConsoleCommand> commands = new ArrayList<>();

    public ConsoleManager(){
        instance = this;
        commands.add(new StopCommand());
        new Thread(this).start();
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()){
            String s = scanner.nextLine();

            for(ConsoleCommand command : commands){
                if(Arrays.stream(command.getAlias()).anyMatch(s1 -> s1.equalsIgnoreCase(s))){
                    command.onExecute(s.replaceFirst(s.split(" ")[0], "").split(" "));
                    continue;
                }
            }

            LOGGER.info("Command not found");
        }
    }

    public static List<ConsoleCommand> getCommands() {
        return instance.commands;
    }
}
