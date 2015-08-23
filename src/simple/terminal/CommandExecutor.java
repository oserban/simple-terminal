package simple.terminal;

import simple.terminal.command.TerminalCommand;

public interface CommandExecutor {
    void execute(String commandName, TerminalCommand.Params values);
}
