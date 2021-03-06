/*
 * Copyright (c) 2019 Jonah Seguin.  All rights reserved.  You may not modify, decompile, distribute or use any code/text contained in this document(plugin) without explicit signed permission from Jonah Seguin.
 * www.jonahseguin.com
 */

package com.jonahseguin.payload.command.commands;

import com.google.inject.Inject;
import com.jonahseguin.payload.PayloadAPI;
import com.jonahseguin.payload.base.Cache;
import com.jonahseguin.payload.base.PayloadPermission;
import com.jonahseguin.payload.command.CmdArgs;
import com.jonahseguin.payload.command.PayloadCommand;

public class CmdCacheList implements PayloadCommand {

    private final PayloadAPI api;

    @Inject
    public CmdCacheList(PayloadAPI api) {
        this.api = api;
    }

    @Override
    public void execute(CmdArgs args) {
        args.msg("&7***** &6Payload Caches &7*****");
        for (Cache cache : api.getCaches().values()) {
            args.msg("&7" + cache.getName() + " - " + cache.getMode().toString().toLowerCase() + " - " + cache.cachedObjectCount() + " objects");
        }
    }

    @Override
    public String name() {
        return "caches";
    }

    @Override
    public String[] aliases() {
        return new String[]{"cs", "cachelist"};
    }

    @Override
    public String desc() {
        return "View active caches";
    }

    @Override
    public PayloadPermission permission() {
        return PayloadPermission.ADMIN;
    }

    @Override
    public String usage() {
        return "";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public int minArgs() {
        return 0;
    }

}
