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

package simple.terminal;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.TerminalFactory;
import simple.terminal.command.LimitedList;
import simple.terminal.command.TerminalCommand;
import simple.terminal.validation.ValidationException;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class BasicTerminal implements TerminalLogger {
    public interface ActionListener {
        void terminalExit();
    }

    private PredictionGenerator predictionGenerator;
    private CommandExecutor commandExecutor;
    private final List<ActionListener> listeners = new LinkedList<>();

    private String cursorText = "_>";
    private Screen screen;
    private TextGraphics textGraphics;
    private int inputLine = 0;
    private int inputCursor = 0;
    private StringBuffer terminalBuffer = new StringBuffer();

    private LimitedList<String> history = new LimitedList<>(10);
    private int historyIndex = 0;

    private boolean running = false;

    public BasicTerminal(PredictionGenerator predictionGenerator, CommandExecutor commandExecutor) throws IOException {
        this.predictionGenerator = predictionGenerator;
        this.commandExecutor = commandExecutor;

        TerminalFactory factory = new DefaultTerminalFactory();
        screen = new TerminalScreen(factory.createTerminal());
    }

    public void addListener(ActionListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(ActionListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void setCursorText(String cursorText) {
        this.cursorText = cursorText;
    }

    private void fireTerminalExit() {
        synchronized (listeners) {
            listeners.forEach(BasicTerminal.ActionListener::terminalExit);
        }
    }

    private void executeCommand(String commandBuffer) {
        String[] parts = commandBuffer.split(" ");
        String command = parts[0];
        String[] values = null;
        if (parts.length > 1) {
            values = Arrays.copyOfRange(parts, 1, parts.length);
        }
        TerminalCommand.Params params = new TerminalCommand.Params(values, this);
        try {
            commandExecutor.execute(command, params);
        } catch (ValidationException e) {
            message(e.getMessage());
        }

    }

    private void resetCursor() throws IOException {
        echoInput();
        inputCursor = Math.min(terminalBuffer.length(), inputCursor);
        screen.setCursorPosition(new TerminalPosition(inputCursor + cursorText.length(), inputLine));
        screen.refresh();
    }

    private void echoInput() {
        textGraphics.putString(0, inputLine, cursorText + terminalBuffer.toString());
    }

    private void executeCommand() {
        String command = terminalBuffer.toString().trim();
        if (command.length() > 0) {
            message(cursorText + command);
            executeCommand(command);
            history.add(command);
            historyIndex = history.size();
        }
    }

    private void message(String message) {
        TerminalSize terminalSize = screen.getTerminalSize();
        while (message.length() > terminalSize.getColumns()) {
            String localMsg = message.substring(0, terminalSize.getColumns());
            message(localMsg);
            message = message.substring(terminalSize.getColumns());
        }
        if (message.length() > 0) {
            textGraphics.putString(0, inputLine++, message);
            if (inputLine > terminalSize.getRows() - 1) {
                screen.scrollLines(0, screen.getTerminalSize().getRows(), 1);
                inputLine--;
            }
        }
    }

    @Override
    public void log(String source, String message) {
        message(source + ": " + message);
    }

    private void executePrediction() throws IOException {
        List<String> predictions = predictionGenerator.generate(terminalBuffer.toString());
        message(cursorText + terminalBuffer.toString());
        predictions.forEach(this::message);
        resetCursor();
    }

    public boolean exitTerminal(TerminalCommand.Params params) {
        message("Exiting terminal ...");
        fireTerminalExit();
        running = false;
        return true;
    }

    public void process() throws IOException {
        if (running) {
            return;
        }

        running = true;
        screen.startScreen();
        textGraphics = screen.newTextGraphics();
        resetCursor();

        while (running) {
            KeyStroke key = screen.readInput();
            if (key != null) {
                switch (key.getKeyType()) {
                    case Character:
                        terminalBuffer.insert(inputCursor, key.getCharacter());
                        inputCursor++;
                        break;
                    case Backspace:
                        if (inputCursor > 0) {
                            int position = cursorText.length() + terminalBuffer.length() - 1;
                            terminalBuffer.deleteCharAt(inputCursor - 1);
                            textGraphics.putString(position, inputLine, " ");
                            inputCursor = Math.max(0, inputCursor - 1);
                        }
                        break;
                    case Delete:
                        if (inputCursor >= 0 && inputCursor < terminalBuffer.length()) {
                            int position = cursorText.length() + terminalBuffer.length() - 1;
                            terminalBuffer.deleteCharAt(inputCursor);
                            textGraphics.putString(position, inputLine, " ");
                        }
                        break;
                    case ArrowUp:
                        historyIndex = Math.max(0, historyIndex - 1);
                        if (history.size() > 0) {
                            terminalBuffer = new StringBuffer(history.get(historyIndex));
                        }
                        break;
                    case ArrowDown:
                        historyIndex = Math.max(0, Math.min(history.size() - 1, historyIndex + 1));
                        if (history.size() > 0) {
                            terminalBuffer = new StringBuffer(history.get(historyIndex));
                        }
                        break;
                    case ArrowLeft:
                        inputCursor = Math.max(0, inputCursor - 1);
                        break;
                    case ArrowRight:
                        inputCursor = Math.min(terminalBuffer.length(), inputCursor + 1);
                        break;
                    case Tab:
                        executePrediction();
                        break;
                    case Enter:
                        executeCommand();
                        terminalBuffer = new StringBuffer();
                        break;
                    case Escape:
                        exitTerminal(null);
                        break;
                }
                resetCursor();
            }
        }
        screen.stopScreen();
    }
}
