package simple.terminal;

import simple.terminal.command.CommandManager;
import simple.terminal.command.TerminalCommand;

import java.io.File;
import java.io.IOException;

public class Main {
    public static final String CMD_PWD = "pwd";
    public static final String CMD_LS = "ls";
    public static final String CMD_CD = "cd";
    public static final String CMD_EXIT = "exit";

    private static File currentRoot = new File(".");

    private static boolean listFolder(TerminalCommand.Params params) {
        TerminalLogger terminalLogger = params.getLogger();
        File[] list = currentRoot.listFiles();
        if (list != null) {
            for (File file : list) {
                terminalLogger.log(CMD_LS, file.getName());
            }
        }
        return true;
    }

    private static boolean pwd(TerminalCommand.Params params) {
        TerminalLogger terminalLogger = params.getLogger();
        terminalLogger.log(CMD_PWD, currentRoot.getAbsolutePath());
        return true;
    }

    private static boolean changeFolder(TerminalCommand.Params params) {
        File newFolder = new File(currentRoot, params.getValues()[0]);
        if (newFolder.isDirectory() && newFolder.canRead()) {
            currentRoot = newFolder;
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        CommandManager commandManager = new CommandManager();
        BasicTerminal terminal = new BasicTerminal(commandManager, commandManager);

        commandManager.addCommand(new TerminalCommand(CMD_CD, Main::changeFolder, 1));
        commandManager.addCommand(new TerminalCommand(CMD_LS, Main::listFolder, 0));
        commandManager.addCommand(new TerminalCommand(CMD_PWD, Main::pwd, 0));
        commandManager.addCommand(new TerminalCommand(CMD_EXIT, terminal::exitTerminal, 0));

        terminal.process();
    }
}
