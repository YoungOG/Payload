/*
 * Copyright (c) 2019 Jonah Seguin.  All rights reserved.  You may not modify, decompile, distribute or use any code/text contained in this document(plugin) without explicit signed permission from Jonah Seguin.
 * www.jonahseguin.com
 */

package com.jonahseguin.payload.database;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.jonahseguin.payload.annotation.Database;
import com.jonahseguin.payload.base.error.ErrorService;
import com.jonahseguin.payload.base.handshake.HandshakeModule;
import com.jonahseguin.payload.server.PayloadServerService;
import com.jonahseguin.payload.server.ServerService;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

public class DatabaseModule extends AbstractModule {

    private final Plugin plugin;
    private final String name;

    public DatabaseModule(@Nonnull Plugin plugin, @Nonnull String name) {
        Preconditions.checkNotNull(plugin);
        Preconditions.checkNotNull(name);
        this.plugin = plugin;
        this.name = name;
    }

    @Override
    protected void configure() {
        bind(String.class).annotatedWith(Database.class).toInstance(name);
        bind(ServerService.class).to(PayloadServerService.class).in(Singleton.class);
        bind(ErrorService.class).annotatedWith(Database.class).to(DatabaseErrorService.class);
        bind(DatabaseService.class).to(PayloadDatabaseService.class).in(Singleton.class);
        install(new HandshakeModule());
    }

}
