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

package io.github.lxgaming.ticket.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.lxgaming.redisvelocity.api.RedisVelocity;
import io.github.lxgaming.ticket.api.Platform;
import io.github.lxgaming.ticket.api.Ticket;
import io.github.lxgaming.ticket.api.util.Logger;
import io.github.lxgaming.ticket.api.util.Reference;
import io.github.lxgaming.ticket.common.TicketImpl;
import io.github.lxgaming.ticket.common.configuration.Config;
import io.github.lxgaming.ticket.common.manager.CommandManager;
import io.github.lxgaming.ticket.velocity.command.BanCommand;
import io.github.lxgaming.ticket.velocity.command.CloseCommand;
import io.github.lxgaming.ticket.velocity.command.CommentCommand;
import io.github.lxgaming.ticket.velocity.command.DebugCommand;
import io.github.lxgaming.ticket.velocity.command.HelpCommand;
import io.github.lxgaming.ticket.velocity.command.OpenCommand;
import io.github.lxgaming.ticket.velocity.command.PardonCommand;
import io.github.lxgaming.ticket.velocity.command.ReadCommand;
import io.github.lxgaming.ticket.velocity.command.ReloadCommand;
import io.github.lxgaming.ticket.velocity.command.ReopenCommand;
import io.github.lxgaming.ticket.velocity.command.TicketCommand;
import io.github.lxgaming.ticket.velocity.command.UserCommand;
import io.github.lxgaming.ticket.velocity.listener.RedisListener;
import io.github.lxgaming.ticket.velocity.listener.VelocityListener;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Plugin(
        id = Reference.ID,
        name = Reference.NAME,
        version = Reference.VERSION,
        description = Reference.DESCRIPTION,
        url = Reference.WEBSITE,
        authors = {Reference.AUTHORS},
        dependencies = {
                @Dependency(id = "location", optional = true),
                @Dependency(id = "redisvelocity", optional = true)
        }
)
public class VelocityPlugin implements Platform {
    
    private static VelocityPlugin instance;
    
    @Inject
    private ProxyServer proxy;
    
    @Inject
    @DataDirectory
    private Path path;
    
    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        instance = this;
        TicketImpl ticket = new TicketImpl(this);
        ticket.getLogger()
                .add(Logger.Level.INFO, LoggerFactory.getLogger(Reference.NAME)::info)
                .add(Logger.Level.WARN, LoggerFactory.getLogger(Reference.NAME)::warn)
                .add(Logger.Level.ERROR, LoggerFactory.getLogger(Reference.NAME)::error)
                .add(Logger.Level.DEBUG, message -> {
                    if (TicketImpl.getInstance().getConfig().map(Config::isDebug).orElse(false)) {
                        LoggerFactory.getLogger(Reference.NAME).info(message);
                    }
                });
        
        ticket.loadTicket();
        
        CommandManager.registerCommand(BanCommand.class);
        CommandManager.registerCommand(CloseCommand.class);
        CommandManager.registerCommand(CommentCommand.class);
        CommandManager.registerCommand(DebugCommand.class);
        CommandManager.registerCommand(HelpCommand.class);
        CommandManager.registerCommand(OpenCommand.class);
        CommandManager.registerCommand(PardonCommand.class);
        CommandManager.registerCommand(ReadCommand.class);
        CommandManager.registerCommand(ReloadCommand.class);
        CommandManager.registerCommand(ReopenCommand.class);
        CommandManager.registerCommand(UserCommand.class);
        getProxy().getCommandManager().register(new TicketCommand(), "ticket");
        getProxy().getEventManager().register(getInstance(), new VelocityListener());
        
        if (getProxy().getPluginManager().isLoaded("redisvelocity")) {
            Ticket.getInstance().getLogger().info("RedisVelocity detected");
            getProxy().getEventManager().register(getInstance(), new RedisListener());
            RedisVelocity.getInstance().registerChannels(Reference.ID);
        }
    }
    
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (getProxy().getPluginManager().isLoaded("redisvelocity")) {
            RedisVelocity.getInstance().unregisterChannels(Reference.ID);
        }
        
        TicketImpl.getInstance().getStorage().close();
        Ticket.getInstance().getLogger().info("{} v{} unloaded", Reference.NAME, Reference.VERSION);
    }
    
    @Override
    public boolean isOnline(UUID uniqueId) {
        if (uniqueId == Platform.CONSOLE_UUID) {
            return true;
        }
        
        return getProxy().getPlayer(uniqueId).map(Player::isActive).orElse(false);
    }
    
    @Override
    public Optional<String> getUsername(UUID uniqueId) {
        if (uniqueId == Platform.CONSOLE_UUID) {
            return Optional.of("CONSOLE");
        }
        
        return getProxy().getPlayer(uniqueId).map(Player::getUsername);
    }
    
    @Override
    public Path getPath() {
        return path;
    }
    
    public static VelocityPlugin getInstance() {
        return instance;
    }
    
    public ProxyServer getProxy() {
        return proxy;
    }
}