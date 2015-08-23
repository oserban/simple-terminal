package simple.terminal;

import simple.terminal.command.CommandManager;
import simple.terminal.command.TerminalCommand;

import java.io.File;
import java.io.IOException;

public class Main {
    private static boolean printRootFolder(TerminalCommand.Params params) {
        File file = new File(".");
        File[] list = file.listFiles();
        if (list != null) {
            for (File result : list) {
                params.getLogger().log("ls", result.toString());
            }
        }
        return true;
    }

    private static boolean printFolder(TerminalCommand.Params params) {
        File file = new File(params.getValues()[0]);
        File[] list = file.listFiles();
        if (list != null) {
            for (File result : list) {
                params.getLogger().log("ls", result.toString());
            }
        }
        return true;
    }

    public static void main(String[] args) throws IOException {
        CommandManager commandManager = new CommandManager();
        commandManager.addCommand(new TerminalCommand("ls", Main::printRootFolder, 0));
        commandManager.addCommand(new TerminalCommand("ls", Main::printFolder, 1));
        BasicTerminal terminal = new BasicTerminal(commandManager, commandManager);
        terminal.process();
    }
}
