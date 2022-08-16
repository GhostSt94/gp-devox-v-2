package com.services;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;
import com.utils.constants.*;

public class Project extends AbstractVerticle {

    private final Logger logger = Logger.getLogger(Project.class);
    private final String COLLECTION_NAME = Collections.PROJECT;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        try {
            vertx.eventBus().consumer(Services.PROJECT_LIST, this::list);
            vertx.eventBus().consumer(Services.PROJECT_CREATE, this::create);
            vertx.eventBus().consumer(Services.PROJECT_MODIFY, this::modify);
            vertx.eventBus().consumer(Services.PROJECT_REMOVE, this::remove);
            vertx.eventBus().consumer(Services.PROJECT_STATS, this::stats);
            startPromise.complete();

        } catch (Exception e) {
            logger.error(e, e);
            startPromise.fail(Exceptions.TECHNICAL_ERROR);
        }

    }


    private void list(Message<JsonObject> message) {
        try {
            logger.debug("consumer -service `" + Services.PROJECT_LIST + "`");

            JsonObject body = message.body();

            JsonObject query = body.getJsonObject("query", new JsonObject());
            JsonObject options = body.getJsonObject("options", new JsonObject());

            JsonObject msg = new JsonObject()
                    .put("collection", COLLECTION_NAME)
                    .put("query", query)
                    .put("options", options);

            String serviceName = Services.DB_FIND;

            if (options.containsKey("withCount"))
                serviceName = Services.DB_FIND_WITH_COUNT;

            vertx.eventBus().request(serviceName, msg, ar -> defaultHandler(ar, message));

        } catch (Exception e) {
            logger.error(e, e);
            message.fail(Exceptions.TECHNICAL_ERROR.failureCode(),
                    Exceptions.TECHNICAL_ERROR.getMessage());
        }
    }

    private void create(Message<JsonObject> message) {
        try {
            logger.debug("consumer -service `" + Services.PROJECT_CREATE + "`");

            JsonObject body = message.body();

            JsonObject msg = new JsonObject()
                    .put("collection", COLLECTION_NAME)
                    .put("query", body);

            vertx.eventBus().<JsonObject>request(Services.DB_INSERT, msg, ar -> {

                if (ar.failed()) {
                    logger.error(ar.cause(), ar.cause());
                    message.fail(((ReplyException) ar.cause()).failureCode(), ar.cause().getMessage());
                } else {
                    JsonObject result = ar.result().body();
                    JsonObject reply = body.copy();
                    reply.put("_id", result.getString("_id"));
                    message.reply(reply);
                }

            });


        } catch (Exception e) {
            logger.error(e, e);
            message.fail(Exceptions.TECHNICAL_ERROR.failureCode(),
                    Exceptions.TECHNICAL_ERROR.getMessage());
        }
    }

    private void modify(Message<JsonObject> message) {
        try {
            logger.debug("consumer -service `" + Services.PROJECT_MODIFY + "`");

            JsonObject body = message.body();

            String projectId = body.getString("_id");

            body.remove("_id");

            JsonObject msg = new JsonObject()
                    .put("collection", COLLECTION_NAME)
                    .put("query", new JsonObject().put("_id", projectId))
                    .put("update", body);

            vertx.eventBus().request(Services.DB_UPDATE, msg, ar -> defaultHandler(ar, message));


        } catch (Exception e) {
            logger.error(e, e);
            message.fail(Exceptions.TECHNICAL_ERROR.failureCode(),
                    Exceptions.TECHNICAL_ERROR.getMessage());
        }
    }

    private void remove(Message<JsonObject> message) {
        try {
            logger.debug("consumer -service `" + Services.PROJECT_REMOVE + "`");

            JsonObject body = message.body();

            String _id = body.getString("_id");

            JsonObject msg = new JsonObject()
                    .put("collection", COLLECTION_NAME)
                    .put("query", new JsonObject().put("_id",_id));

            vertx.eventBus().request(Services.DB_REMOVE_DOCUMENT, msg, ar -> defaultHandler(ar, message));

        } catch (Exception e) {
            logger.error(e, e);
            message.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
        }
    }

    private void stats(Message<JsonObject> message) {
        try {
            logger.debug("consumer -service `" + Services.PROJECT_STATS + "`");

            JsonObject msg = new JsonObject()
                    .put("collection", COLLECTION_NAME)
                    .put("query", new JsonObject());

            String serviceName = Services.DB_COUNT;

            vertx.eventBus().request(serviceName, msg, ar -> defaultHandler(ar, message));

        } catch (Exception e) {
            logger.error(e, e);
            message.fail(Exceptions.TECHNICAL_ERROR.failureCode(),
                    Exceptions.TECHNICAL_ERROR.getMessage());
        }
    }

    private void defaultHandler(AsyncResult<Message<Object>> ar, Message<JsonObject> message) {
        if (ar.failed()) {
            logger.error(ar.cause(), ar.cause());
            message.fail(((ReplyException) ar.cause()).failureCode(), ar.cause().getMessage());
        } else {
            message.reply(ar.result().body());
        }
    }
}

