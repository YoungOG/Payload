/*
 * Copyright (c) 2019 Jonah Seguin.  All rights reserved.  You may not modify, decompile, distribute or use any code/text contained in this document(plugin) without explicit signed permission from Jonah Seguin.
 * www.jonahseguin.com
 */

package com.jonahseguin.payload.base;

import com.jonahseguin.payload.PayloadAPI;
import com.jonahseguin.payload.PayloadMode;
import com.jonahseguin.payload.base.error.ErrorService;
import com.jonahseguin.payload.base.lang.LangService;
import com.jonahseguin.payload.base.network.NetworkPayload;
import com.jonahseguin.payload.base.network.NetworkService;
import com.jonahseguin.payload.base.settings.CacheSettings;
import com.jonahseguin.payload.base.store.PayloadStore;
import com.jonahseguin.payload.base.sync.SyncMode;
import com.jonahseguin.payload.base.sync.SyncService;
import com.jonahseguin.payload.base.type.Payload;
import com.jonahseguin.payload.base.type.PayloadController;
import com.jonahseguin.payload.base.type.PayloadInstantiator;
import com.jonahseguin.payload.database.DatabaseDependent;
import com.jonahseguin.payload.database.DatabaseService;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface Cache<K, X extends Payload<K>, N extends NetworkPayload<K>> extends Service, DatabaseDependent {

    Optional<N> getNetworked(@Nonnull K key);

    Optional<N> getNetworked(@Nonnull X payload);

    Optional<X> get(@Nonnull K key);

    Future<Optional<X>> getAsync(@Nonnull K key);

    Optional<X> getFromCache(@Nonnull K key);

    Optional<X> getFromDatabase(@Nonnull K key);

    boolean save(@Nonnull X payload);

    Future<Boolean> saveAsync(@Nonnull X payload);

    boolean saveNoSync(@Nonnull X payload);

    void cache(@Nonnull X payload);

    void uncache(@Nonnull K key);

    void uncache(@Nonnull X payload);

    void delete(@Nonnull K key);

    void delete(@Nonnull X payload);

    boolean isCached(@Nonnull K key);

    void prepareUpdate(@Nonnull X payload, @Nonnull PayloadCallback<Optional<X>> callback);

    void prepareUpdateAsync(@Nonnull X payload, @Nonnull PayloadCallback<Optional<X>> callback);

    void cacheAll();

    @Nonnull
    Collection<X> getAll();

    @Nonnull
    Collection<X> getCached();

    @Nonnull
    NetworkService<K, X, N> getNetworkService();

    @Nonnull
    SyncService<K, X, N> getSyncService();

    @Nonnull
    ErrorService getErrorService();

    void setErrorService(@Nonnull ErrorService errorService);

    @Nonnull
    CacheSettings getSettings();

    int saveAll();

    @Nonnull
    PayloadStore<K, X> getLocalStore();

    @Nonnull
    PayloadStore<K, X> getDatabaseStore();

    @Nonnull
    String getName();

    @Nonnull
    String getServerSpecificName();

    @Nonnull
    Plugin getPlugin();

    @Nonnull
    PayloadMode getMode();

    void setMode(@Nonnull PayloadMode mode);

    void setInstantiator(@Nonnull PayloadInstantiator<K, X> instantiator);

    String keyToString(@Nonnull K key);

    K keyFromString(@Nonnull String key);

    void addDepend(@Nonnull Cache cache);

    boolean isDependentOn(@Nonnull Cache cache);

    @Nonnull
    PayloadAPI getApi();

    void runAsync(@Nonnull Runnable runnable);

    @Nonnull
    <T> Future<T> runAsync(@Nonnull Callable<T> callable);

    @Nonnull
    LangService getLang();

    boolean isDebug();

    void setDebug(boolean debug);

    void alert(@Nonnull PayloadPermission required, @Nonnull String msg);

    @Nonnull
    SyncMode getSyncMode();

    void setSyncMode(@Nonnull SyncMode mode);

    void updatePayloadID();

    int cachedObjectCount();

    @Nonnull
    PayloadController<X> controller(@Nonnull K key);

    DatabaseService getDatabase();

    X create();

    N createNetworked();

}

