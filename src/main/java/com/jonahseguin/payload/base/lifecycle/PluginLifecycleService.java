/*
 * Copyright (c) 2019 Jonah Seguin.  All rights reserved.  You may not modify, decompile, distribute or use any code/text contained in this document(plugin) without explicit signed permission from Jonah Seguin.
 * www.jonahseguin.com
 */

package com.jonahseguin.payload.base.lifecycle;

import com.google.inject.Inject;
import com.jonahseguin.payload.annotation.Database;
import com.jonahseguin.payload.base.Service;
import com.jonahseguin.payload.base.error.ErrorService;
import org.bukkit.plugin.Plugin;

public class PluginLifecycleService implements LifecycleService {

    private final Plugin plugin;
    private final ErrorService errorService;

    @Inject
    public PluginLifecycleService(Plugin plugin, @Database ErrorService errorService) {
        this.plugin = plugin;
        this.errorService = errorService;
    }

    @Override
    public boolean start(Service service) {
        if (service == null) {
            return false;
        }
        if (service.isRunning()) return true;
        boolean success = true;
        try {
            if (!service.start()) {
                success = false;
            }
        } catch (Exception ex) {
            errorService.capture(ex, "Error starting service: " + service.getClass().getSimpleName() + ": " + ex.getMessage());
            success = false;
        } finally {
            if (!success) {
                plugin.getServer().getPluginManager().disablePlugin(plugin);
            }
        }
        return success;
    }

    @Override
    public boolean shutdown(Service service) {
        if (service == null) {
            return false;
        }
        if (!service.isRunning()) return true;
        try {
            if (!service.shutdown()) {
                errorService.capture("Error shutting down service: " + service.getClass().getSimpleName());
                return false;
            } else {
                return true;
            }
        } catch (Exception ex) {
            errorService.capture(ex, "Error shutting down service: " + service.getClass().getSimpleName() + ": " + ex.getMessage());
            return false;
        }
    }
}
