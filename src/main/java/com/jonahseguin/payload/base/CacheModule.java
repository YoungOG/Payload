/*
 * Copyright (c) 2019 Jonah Seguin.  All rights reserved.  You may not modify, decompile, distribute or use any code/text contained in this document(plugin) without explicit signed permission from Jonah Seguin.
 * www.jonahseguin.com
 */

package com.jonahseguin.payload.base;

import com.google.inject.AbstractModule;

public class CacheModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PayloadCacheService.class).to(PayloadDatabaseCacheService.class);
    }



}
