/*
 * Copyright (c) AgentSlang Project Maintainers
 *                                web: http://agent.roboslang.org/
 * All Rights Reserved. Use is subject to license terms.
 *
 * The usage of this project makes mandatory the authors citation in
 * any scientific publication or technical reports. For websites or
 * research projects the AgentSlang website and logo needs to be linked
 * in a visible area. Please check the project website for more details.
 *
 * All the files of the AgentSlang Project are subject of this license,
 * until stated otherwise. All the libraries, sounds and graphic elements
 * used in the project are subject to their own license.
 *
 * AgentSlang and its sub-projects (AgentSlang, MyBlock and Syn!bad)
 * are free software: you can redistribute them and/or modify
 * them under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package simple.terminal.command;

import simple.terminal.CommandExecutor;
import simple.terminal.PredictionGenerator;
import simple.terminal.validation.ValidationException;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class CommandManager implements PredictionGenerator, CommandExecutor {
    private final Map<String, List<TerminalCommand>> commandMap = new HashMap<>();

    public void addCommand(TerminalCommand command) {
        String commandName = command.getName();
        addSubCommands(commandName, command);
    }

    private void addSubCommands(String commandName, TerminalCommand command) {
        addCommand("", command);
        for (int length = 1; length <= commandName.length(); length++) {
            String substring = commandName.substring(0, length);
            addCommand(substring, command);
        }
    }

    private void addCommand(String commandName, TerminalCommand command) {
        List<TerminalCommand> list = commandMap.get(commandName);
        if (list == null) {
            list = new LinkedList<>();
            commandMap.put(commandName, list);
        }
        list.add(command);
    }

    public List<TerminalCommand> estimate(String partialCommand, int limit) {
        List<TerminalCommand> list = commandMap.get(partialCommand);
        if (list == null) {
            return Collections.emptyList();
        } else {
            return list.stream().limit(limit).collect(toList());
        }
    }

    @Override
    public List<String> generate(String currentBuffer) {
        List<TerminalCommand> predictions = estimate(currentBuffer, 10);
        return predictions.stream().map(TerminalCommand::toString).collect(toList());
    }

    public void execute(String commandName, TerminalCommand.Params values) throws ValidationException {
        List<TerminalCommand> list = commandMap.get(commandName);
        if (list == null) {
            throw new ValidationException("Command not found!");
        } else {
            List<TerminalCommand> commands = list.stream().filter(c -> c.getName().equals(commandName)).collect(toList());
            if (commands.isEmpty()) {
                throw new ValidationException("Command not found!");
            } else {
                commands = list.stream().filter(c -> c.validate(values.getValues())).collect(toList());
                if (commands.isEmpty()) {
                    throw new ValidationException("Command found, but the parameter arity is not right!");
                } else if (commands.size() > 1) {
                    Optional<TerminalCommand> command = commands.stream().filter(TerminalCommand::isDetermined).findFirst();
                    if (!command.isPresent() && command.get().executeCommand(values)) {
                        throw new ValidationException("Command execution failed!");
                    }
                } else {
                    if (!commands.get(0).executeCommand(values)) {
                        throw new ValidationException("Command execution failed!");
                    }
                }
            }
        }
    }
}
