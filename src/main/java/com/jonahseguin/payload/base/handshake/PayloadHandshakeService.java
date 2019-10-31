package com.jonahseguin.payload.base.handshake;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.jonahseguin.payload.database.DatabaseService;
import org.bson.Document;
import redis.clients.jedis.Jedis;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PayloadHandshakeService implements HandshakeService {

    private final ConcurrentMap<String, HandshakeContainer> containers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, HandshakeController> replyControllers = new ConcurrentHashMap<>();
    private final Injector injector;
    private final DatabaseService database;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Inject
    public PayloadHandshakeService(Injector injector, DatabaseService database) {
        this.injector = injector;
        this.database = database;
    }

    @Override
    public void receiveReply(@Nonnull String channel, @Nonnull HandshakeData data) {
        if (replyControllers.containsKey(data.getID())) {
            HandshakeController controller = replyControllers.get(data.getID());
            if (controller != null) {
                executor.submit(() -> {
                    controller.load(data);
                    controller.executeHandler(data);
                });
            }
            replyControllers.remove(data.getID());
        }
    }

    @Override
    public void receive(@Nonnull String channel, @Nonnull HandshakeData data) {
        // Receiving before sending reply
        HandshakeContainer container = containers.get(channel);
        HandshakeController controller = container.createInstance();
        executor.submit(() -> {
            controller.load(data);
            controller.receive();
            try (Jedis jedis = database.getJedisResource()) {
                jedis.publish(controller.channelReply(), data.getDocument().toJson());
            } catch (Exception ex) {
                database.getErrorService().capture(ex, "Error with Jedis resource during handshake receive (sending reply) for " + controller.getClass().getSimpleName());
            }
        });
    }

    @Override
    public <H extends HandshakeController> void subscribe(@Nonnull Class<H> type) {
        Preconditions.checkNotNull(type);
        HandshakeContainer<H> container = new HandshakeContainer<>(type, injector);
        H controller = container.getSubscriberController();
        containers.put(controller.channelPublish(), container);
        containers.put(controller.channelReply(), container);
        executor.submit(controller::listen);
    }

    @Override
    public <H extends HandshakeController> HandshakeHandler<H> publish(@Nonnull H controller) {
        Preconditions.checkNotNull(controller);
        HandshakeData data = new HandshakeData(new Document());
        data.writeID();
        controller.write(data);
        HandshakeHandler<H> handler = new HandshakeHandler<>(data);
        controller.setHandler(handler);
        replyControllers.put(data.getID(), controller);
        executor.submit(() -> {
            try (Jedis jedis = database.getJedisResource()) {
                jedis.publish(controller.channelPublish(), data.getDocument().toJson());
            } catch (Exception ex) {
                database.getErrorService().capture(ex, "Error with Jedis resource during handshake publish for " + controller.getClass().getSimpleName());
            }
        });
        return handler;
    }
}
