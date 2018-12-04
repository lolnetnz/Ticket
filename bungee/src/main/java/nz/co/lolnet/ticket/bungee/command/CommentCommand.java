/*
 * Copyright 2018 lolnet.co.nz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.co.lolnet.ticket.bungee.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import nz.co.lolnet.ticket.api.data.CommentData;
import nz.co.lolnet.ticket.api.data.TicketData;
import nz.co.lolnet.ticket.api.data.UserData;
import nz.co.lolnet.ticket.bungee.util.BungeeToolbox;
import nz.co.lolnet.ticket.common.command.AbstractCommand;
import nz.co.lolnet.ticket.common.manager.DataManager;
import nz.co.lolnet.ticket.common.util.Toolbox;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.List;

public class CommentCommand extends AbstractCommand {
    
    public CommentCommand() {
        addAlias("comment");
        addAlias("comments");
        setPermission("ticket.command.comment");
        setUsage("<Id> [Message]");
    }
    
    @Override
    public void execute(Object object, List<String> arguments) {
        CommandSender sender = (CommandSender) object;
        
        if (arguments.isEmpty()) {
            sender.sendMessage(BungeeToolbox.getTextPrefix().append("Invalid arguments: " + getUsage()).color(ChatColor.RED).create());
            return;
        }
        
        Integer ticketId = Toolbox.parseInteger(arguments.remove(0)).orElse(null);
        if (ticketId == null) {
            sender.sendMessage(BungeeToolbox.getTextPrefix().append("Failed to parse argument").color(ChatColor.RED).create());
            return;
        }
        
        String message = String.join(" ", arguments);
        if (StringUtils.isBlank(message)) {
            sender.sendMessage(BungeeToolbox.getTextPrefix().append("Message cannot be blank").color(ChatColor.RED).create());
            return;
        }
        
        if (message.length() > 256) {
            sender.sendMessage(BungeeToolbox.getTextPrefix().append("Message length may not exceed 256").color(ChatColor.RED).create());
            return;
        }
        
        UserData user = DataManager.getOrCreateUser(BungeeToolbox.getUniqueId(sender)).orElse(null);
        if (user == null) {
            sender.sendMessage(BungeeToolbox.getTextPrefix().append("An error has occurred. Details are available in console.").color(ChatColor.RED).create());
            return;
        }
        
        TicketData ticket = DataManager.getTicket(ticketId).orElse(null);
        if (ticket == null) {
            sender.sendMessage(BungeeToolbox.getTextPrefix().append("Ticket doesn't exist").color(ChatColor.RED).create());
            return;
        }
        
        CommentData comment = DataManager.createComment(ticket.getId(), user.getUniqueId(), Instant.now(), message).orElse(null);
        if (comment == null) {
            sender.sendMessage(BungeeToolbox.getTextPrefix().append("An error has occurred. Details are available in console.").color(ChatColor.RED).create());
            return;
        }
        
        sender.sendMessage(BungeeToolbox.getTextPrefix().append("You added a comment to ticket #" + ticket.getId()).color(ChatColor.GOLD).create());
    }
}