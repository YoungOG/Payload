package com.jonahseguin.payload.mode.profile.layer;

import com.jonahseguin.payload.base.exception.PayloadLayerCannotProvideException;
import com.jonahseguin.payload.mode.profile.PayloadProfile;
import com.jonahseguin.payload.mode.profile.ProfileCache;
import com.jonahseguin.payload.mode.profile.ProfileData;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Getter
public class ProfileLayerLocal<X extends PayloadProfile> extends ProfileCacheLayer<X> {

    private final ConcurrentMap<UUID, X> localCache = new ConcurrentHashMap<>();

    public ProfileLayerLocal(ProfileCache<X> cache) {
        super(cache);
    }

    @Override
    public X get(ProfileData data) throws PayloadLayerCannotProvideException {
        return get(data.getUniqueId());
    }

    @Override
    public X get(UUID uuid) throws PayloadLayerCannotProvideException {
        if (!this.has(uuid)) {
            throw new PayloadLayerCannotProvideException("Cannot provide (does not have) in local layer for Profile UUID:" + uuid, this.cache);
        }
        X x = this.localCache.get(uuid);
        x.interact();
        return x;
    }

    @Override
    public boolean save(X payload) {
        payload.interact();
        this.localCache.put(payload.getUniqueId(), payload);
        return true;
    }

    @Override
    public boolean has(UUID key) {
        return this.localCache.containsKey(key);
    }

    @Override
    public void remove(UUID key) {
        this.localCache.remove(key);
    }

    @Override
    public boolean has(ProfileData data) {
        return this.localCache.containsKey(data.getUniqueId());
    }

    @Override
    public boolean has(X payload) {
        payload.interact();
        return this.localCache.containsKey(payload.getUniqueId());
    }

    @Override
    public void remove(ProfileData data) {
        this.localCache.remove(data.getUniqueId());
    }

    @Override
    public void remove(X payload) {
        this.localCache.remove(payload.getUniqueId());
    }

    @Override
    public int cleanup() {
        long expiryTimeSeconds = this.getCache().getSettings().getLocalExpiryTimeSeconds();
        Set<UUID> purge = new HashSet<>();
        for (Map.Entry<UUID, X> entry : this.localCache.entrySet()) {
            if (entry.getValue().getLastInteractionTimestamp() < (System.currentTimeMillis() - (expiryTimeSeconds * 1000))) {
                // Expired
                purge.add(entry.getKey());
            }
        }

        for (UUID key : purge) {
            this.localCache.remove(key);
        }

        return purge.size();
    }

    @Override
    public long clear() {
        long i = this.localCache.size();
        this.localCache.clear();
        return i;
    }

    @Override
    public Collection<X> getAll() {
        return this.localCache.values();
    }

    @Override
    public long size() {
        return this.localCache.size();
    }

    @Override
    public void init() {
        // Nothing to initialize
    }

    @Override
    public void shutdown() {
        this.clear(); // For memory purposes
    }

    @Override
    public String layerName() {
        return "Profile Local";
    }

    @Override
    public boolean isDatabase() {
        return false;
    }
}