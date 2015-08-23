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

import simple.terminal.TerminalLogger;

import java.util.Arrays;
import java.util.function.Function;

public class TerminalCommand {
    public static class Params {
        private String[] values;
        private TerminalLogger logger;

        public Params(String[] values, TerminalLogger logger) {
            this.values = values;
            this.logger = logger;
        }

        public String[] getValues() {
            return values;
        }

        public TerminalLogger getLogger() {
            return logger;
        }

        @Override
        public String toString() {
            return Arrays.toString(values);
        }
    }

    private String name;
    private int arity = -1;
    private boolean varargs = false;
    private Function<Params, Boolean> externalCommand;

    public TerminalCommand(String name, Function<Params, Boolean> externalCommand, int arity) {
        this.name = name;
        this.arity = arity;
        this.varargs = false;
        this.externalCommand = externalCommand;
    }

    public TerminalCommand(String name, Function<Params, Boolean> externalCommand, boolean varargs) {
        this.name = name;
        this.arity = -1;
        this.varargs = varargs;
        this.externalCommand = externalCommand;
    }

    public String getName() {
        return name;
    }

    public boolean executeCommand(Params params) {
        if (validate(params.values)) {
            params.values = params.values == null ? new String[0] : params.values;
            return externalCommand.apply(params);
        } else {
            return false;
        }
    }

    public boolean isDetermined() {
        return !varargs;
    }

    public boolean validate(String[] values) {
        if (varargs) {
            return true;
        } else if (arity <= 0) {
            return values == null || values.length == 0;
        } else {
            return values != null && values.length == arity;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" (");
        if (varargs) {
            sb.append("varargs");
        } else {
            sb.append(arity).append(" params");
        }
        sb.append(")");
        return sb.toString();
    }
}
