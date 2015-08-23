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

public interface TerminalLogger {
    void log(String source, String message);
}
