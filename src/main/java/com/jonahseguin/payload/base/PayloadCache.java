package com.jonahseguin.payload.base;

import com.jonahseguin.payload.PayloadHook;
import com.jonahseguin.payload.PayloadMode;
import com.jonahseguin.payload.PayloadPlugin;
import com.jonahseguin.payload.base.error.DefaultErrorHandler;
import com.jonahseguin.payload.base.error.PayloadErrorHandler;
import com.jonahseguin.payload.base.lang.PLang;
import com.jonahseguin.payload.base.lang.PayloadLangController;
import com.jonahseguin.payload.base.layer.LayerController;
import com.jonahseguin.payload.base.state.CacheState;
import com.jonahseguin.payload.base.state.PayloadTaskExecutor;
import com.jonahseguin.payload.base.type.Payload;
import com.jonahseguin.payload.base.type.PayloadData;
import com.jonahseguin.payload.database.DatabaseDependent;
import com.jonahseguin.payload.database.PayloadDatabase;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The abstract backbone of all Payload cache systems.
 * All Caching modes (profile, object, simple) extend this class.
 */
@Getter
public abstract class PayloadCache<K, X extends Payload, D extends PayloadData> implements DatabaseDependent {

    protected final transient Plugin plugin; // The Bukkit JavaPlugin that created this cache.  non-persistent

    protected String name; // The name for this payload cache

    protected transient boolean debug = false; // Debug for this cache

    protected transient PayloadErrorHandler errorHandler = new DefaultErrorHandler();
    protected transient PayloadDatabase payloadDatabase = null;
    protected transient PayloadMode mode = PayloadMode.STANDALONE; // Payload Mode for this cache

    protected transient final ExecutorService pool = Executors.newCachedThreadPool();
    protected transient final PayloadTaskExecutor<K, X> executor;
    protected transient final PayloadLangController langController = new PayloadLangController();
    protected transient final CacheState<K, X> state;
    protected transient final LayerController<X, D> layerController = new LayerController<>();

    protected transient final Class<K> keyType;
    protected transient final Class<X> valueType;

    public PayloadCache(final PayloadHook hook, final String name, Class<K> keyType, Class<X> valueType) {
        if (hook.getPlugin() == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (hook.getPlugin() instanceof PayloadPlugin) {
            throw new IllegalArgumentException("Plugin cannot be PayloadPlugin");
        }
        if (!hook.isValid()) {
            throw new IllegalStateException("Provided PayloadHook is not valid; cannot create cache '" + name + "'");
        }
        this.keyType = keyType;
        this.valueType = valueType;
        this.plugin = hook.getPlugin();
        this.name = name;
        this.executor = new PayloadTaskExecutor<>(this);
        this.state = new CacheState<>(this);
    }

    public void alert(PayloadPermission required, PLang lang, String... args) {
        Bukkit.getLogger().info(this.langController.get(lang, args));
        for (Player pl : this.plugin.getServer().getOnlinePlayers()) {
            if (required.has(pl)) {
                pl.sendMessage(this.langController.get(lang, args));
            }
        }
    }

    public final boolean isLocked() {
        return this.state.isLocked() || PayloadPlugin.get().isLocked();
    }

    public final boolean start() {
        // What else should be implemented here?
        this.init();
        return true;
    }

    public final boolean stop() {
        this.shutdown(); // Allow the implementing cache to do it's shutdown first
        this.pool.shutdown(); // Shutdown our thread pool
        if (this.save()) {
            return true;
        } else {
            // Failed to save
            getErrorHandler().error(this.name, "Failed to save during shutdown");
            return false;
        }
    }

    protected final boolean save() {
        try {
            payloadDatabase.getDatastore().save(this);
            return true;
        }
        catch (Exception ex) {
            this.getErrorHandler().exception(this.name, ex, "Failed to save cache");
            return false;
        }
    }

    public final void setupDatabase(PayloadDatabase database) {
        if (this.payloadDatabase != null) {
            throw new IllegalStateException("Database has already been defined");
        }
        this.payloadDatabase = database;
    }

    /**
     * Starts up & initializes the cache.
     * Prepares everything for a fresh startup, ensures database connections, etc.
     */
    protected abstract void init();

    /**
     * Shut down the cache.
     * Saves everything first, and safely shuts down
     */
    protected abstract void shutdown();


    /**
     * Get an object stored in this cache, using the best method provided by the cache
     * @param key The key to use to get the object (i.e a string, number, etc.)
     * @return The object if available (else null)
     */
    protected abstract X get(K key);


    /**
     * Get the name of this cache (set by the end user, should be unique)
     * A {@link com.jonahseguin.payload.base.exception.DuplicateCacheException} error will be thrown if another cache
     * exists with the same name or ID during creation.
     *
     * @return String: Cache Name
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Get the JavaPlugin controlling this cache
     * Every Payload cache must be associated with a JavaPlugin for event handling and etc.
     *
     * @return Plugin
     */
    public final Plugin getPlugin() {
        return this.plugin;
    }

    /**
     * Get the current Mode this cache is functioning in
     * There are two modes: STANDALONE, or NETWORK_NODE
     * In standalone mode, a cache functions as it's own entity and will use login/logout events to handle caching normally
     * In contrast, in network node mode, a cache functions as a node in a BungeeCord/etc. proxied network,
     * where in such logins are handled before logouts, data is transferred through a handshake via Redis pub/sub if
     * a player is already logged into another node.
     *
     * @return {@link PayloadMode} the current mode
     */
    public PayloadMode getMode() {
        return mode;
    }

    /**
     * Set the current Mode this cache is functioning in
     * Two modes: STANDALONE, or NETWORK_NODE
     *
     * @param mode {@link PayloadMode} mode
     * @see #getMode()
     */
    public void setMode(PayloadMode mode) {
        this.mode = mode;
    }

    @Override
    public void onMongoDbDisconnect() {
        this.getPayloadDatabase().getState().setMongoConnected(false);
        this.getState().lock();
    }

    @Override
    public void onRedisDisconnect() {
        this.getPayloadDatabase().getState().setRedisConnected(false);
        this.getState().lock();
    }

    @Override
    public void onMongoDbReconnect() {
        this.getPayloadDatabase().getState().setMongoConnected(true);
        if (this.getPayloadDatabase().getState().isDatabaseConnected()) {
            // Both connected
            this.getState().unlock();
        }
    }

    @Override
    public void onRedisReconnect() {
        this.getPayloadDatabase().getState().setRedisConnected(true);
        if (this.getPayloadDatabase().getState().isDatabaseConnected()) {
            // Both connected
            this.getState().unlock();
        }
    }

    @Override
    public void onMongoDbInitConnect() {
        this.getPayloadDatabase().getState().setMongoConnected(true);
        if (this.getPayloadDatabase().getState().isDatabaseConnected()) {
            // Both connected
            this.getState().unlock();
        }
    }

    @Override
    public void onRedisInitConnect() {
        this.getPayloadDatabase().getState().setRedisConnected(true);
        if (this.getPayloadDatabase().getState().isDatabaseConnected()) {
            // Both connected
            this.getState().unlock();
        }
    }

    @Override
    public boolean requireRedis() {
        return true;
    }

    @Override
    public boolean requireMongoDb() {
        return true;
    }
}
