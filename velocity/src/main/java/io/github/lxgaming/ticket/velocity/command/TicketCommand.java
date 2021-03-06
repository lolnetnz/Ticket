/*
 * Copyright 2018 Alex Thomson
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

package io.github.lxgaming.ticket.velocity.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import io.github.lxgaming.ticket.api.Ticket;
import io.github.lxgaming.ticket.api.util.Reference;
import io.github.lxgaming.ticket.common.command.AbstractCommand;
import io.github.lxgaming.ticket.common.manager.CommandManager;
import io.github.lxgaming.ticket.velocity.util.VelocityToolbox;
import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class TicketCommand implements Command {
    
    @Override
    public void execute(CommandSource source, String[] args) {
        List<String> arguments = Lists.newArrayList(args);
        AbstractCommand command = CommandManager.getChildCommand(arguments).orElse(null);
        if (command == null) {
            TextComponent.Builder textBuilder = TextComponent.builder("");
            textBuilder.append(TextComponent.of("Use ", TextColor.BLUE));
            textBuilder.append(TextComponent.of("/" + Reference.ID + " help ", TextColor.GREEN));
            textBuilder.append(TextComponent.of("to view available commands.", TextColor.BLUE));
            source.sendMessage(textBuilder.build());
            return;
        }
        
        if (StringUtils.isBlank(command.getPermission()) || !source.hasPermission(command.getPermission())) {
            source.sendMessage(TextComponent.of("You do not have permission to execute this command!", TextColor.RED));
            return;
        }
        
        Ticket.getInstance().getLogger().debug("Processing {}", command.getPrimaryAlias().orElse("Unknown"));
        
        try {
            command.execute(source, arguments);
        } catch (Throwable throwable) {
            Ticket.getInstance().getLogger().error("Encountered an error while executing {}", command.getClass().getSimpleName(), throwable);
            source.sendMessage(VelocityToolbox.getTextPrefix().append(TextComponent.of("An error has occurred. Details are available in console.", TextColor.RED)));
        }
    }
    
    @Override
    public List<String> suggest(CommandSource source, String[] currentArgs) {
        return ImmutableList.of();
    }
    
    @Override
    public boolean hasPermission(CommandSource source, String[] args) {
        return true;
    }
}