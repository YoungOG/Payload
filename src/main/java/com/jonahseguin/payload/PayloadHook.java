package com.jonahseguin.payload;


import com.jonahseguin.payload.database.PayloadDatabase;
import com.jonahseguin.payload.mode.object.ObjectCache;
import com.jonahseguin.payload.mode.object.PayloadObject;
import com.jonahseguin.payload.mode.profile.PayloadProfile;
import com.jonahseguin.payload.mode.profile.ProfileCache;
import org.bukkit.plugin.Plugin;


public class PayloadHook {

    private final Plugin plugin;

    PayloadHook(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public <X extends PayloadProfile> ProfileCache<X> createProfileCache(PayloadDatabase database, String name, Class<X> type) {
        ProfileCache<X> cache = new ProfileCache<>(this, name, type);
        database.hookCache(cache);
        PayloadAPI.get().saveCache(cache, this);

        database.getMorphia().map(type);

        return cache;
    }

    public <X extends PayloadObject> ObjectCache<X> createObjectCache(PayloadDatabase database, String name, Class<X> type) {
        ObjectCache<X> cache = new ObjectCache<>(this, name, type);
        database.hookCache(cache);
        PayloadAPI.get().saveCache(cache, this);

        database.getMorphia().map(type);

        return cache;
    }

    public final boolean isValid() {
        return PayloadAPI.get().validateHook(this.plugin, this);
    }

}
