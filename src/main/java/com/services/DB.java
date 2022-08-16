package com.services;

import com.utils.constants.Exceptions;
import com.utils.constants.Fields;
import com.utils.constants.Services;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.BulkOperation;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class DB extends AbstractVerticle {

	private final Logger logger = Logger.getLogger(DB.class);

	@Override
	public void start(Promise<Void> startFuture) {
		try {

			// init client BD
			MongoClient client = MongoClient.createShared(vertx, config().getJsonObject("db"));

			// creation d'un eb consumer pour trouver les données sur l'adresse `db.find`
			vertx.eventBus().consumer(Services.DB_FIND, hdlr -> {
				final String op_id = System.currentTimeMillis() + "-" + Math.round(Math.random() * 100);
				try {
					JsonObject msg = (JsonObject) hdlr.body();
					logger.debug("consumer -service `db.find`- {" + op_id + "}, msg: " + msg);

					String collection = msg.getString("collection");
					JsonObject query = msg.getJsonObject("query");
					JsonObject options = msg.getJsonObject("options", new JsonObject());

					FindOptions findOptions = new FindOptions();

					int limit = options.getInteger("limit", -1);
					int page = options.getInteger("page", 0);
					// subtract 1 from page cause skip = page * limit
					page = page != 0 ? page - 1 : page;

					findOptions.setLimit(limit);
					findOptions.setSkip(page * limit);
					if (options.containsKey("sort"))
						findOptions.setSort(options.getJsonObject("sort"));
					findOptions.setFields(options.getJsonObject("fields", new JsonObject()));

					if (options.containsKey("sort") && !options.getJsonObject("sort").isEmpty())
						findOptions.setSort(options.getJsonObject("sort"));

					logger.debug("db `find` {" + op_id + "} collection: " + collection + ", query: " + query + ", findOptions: " + findOptions.toJson());

					client.findWithOptions(collection, query, findOptions, hdlrFind -> {
						if (hdlrFind.succeeded()) {
							JsonArray reply = new JsonArray(hdlrFind.result());
							logger.debug("db `find` {" + op_id + "} succeeded, reply: " + reply);
							hdlr.reply(reply);
						} else {
							logger.error(hdlrFind.cause(), hdlrFind.cause());
							hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
						}
					});

				} catch (Exception e) {
					logger.error(e, e);
					hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
				}
			});

			// creation d'un eb consumer pour trouver et compter le nombre des donnees
			vertx.eventBus().consumer(Services.DB_FIND_WITH_COUNT, hdlr -> {
				final String op_id = System.currentTimeMillis() + "-" + Math.round(Math.random() * 100);
				try {
					JsonObject msg = (JsonObject) hdlr.body();
					logger.debug("consumer -service `db.findWithCount(aggregate)`- {" + op_id + "}, msg: " + msg);


					String collection = msg.getString("collection");
					JsonObject query = msg.getJsonObject("query");
					JsonObject options = msg.getJsonObject("options", new JsonObject());

					int limit = options.getInteger("limit", -1);
					int page = options.getInteger("page", 0);
					page = page != 0 ? page - 1 : page;


					JsonArray docs_stages = new JsonArray();
					if (options.containsKey("sort") && !options.getJsonObject("sort").isEmpty())
						docs_stages.add(new JsonObject().put("$sort", options.getJsonObject("sort")));

					docs_stages.add(new JsonObject().put("$skip", page * limit));

					if (options.containsKey("limit") && limit > 0)
						docs_stages.add(new JsonObject().put("$limit", limit));

					if (options.containsKey("fields") && !options.getJsonObject("fields").isEmpty())
						docs_stages.add(new JsonObject().put("$project", options.getJsonObject("fields")));

					// get stages
					JsonArray pipeline = new JsonArray()
							.add(new JsonObject()
									.put("$match", query)
							)
							.add(new JsonObject()
									.put("$facet", new JsonObject()
											// count aggregation stages
											.put("count", new JsonArray()
													.add(new JsonObject().put("$count", "total")))
											// docs list aggregation stages
											.put("docs", docs_stages)
									)
							)
							.add(new JsonObject()
									.put("$unwind", "$count")
							)
							.add(new JsonObject().put("$project", new JsonObject()
									.put("count", "$count.total")
									.put("docs", "$docs"))
							);

					// set command
					JsonObject command = new JsonObject()
							.put("aggregate", collection)
							.put("pipeline", pipeline)
							.put("allowDiskUse", true)
							.put("explain", false);

					if (msg.containsKey("batchsize"))
						command.put("cursor", new JsonObject().put("batchSize", msg.getInteger("batchsize")));

					logger.debug("db `findWithCount(aggregate)` {" + op_id + "} command: " + command);

					client.runCommand("aggregate", command, res -> {
						if (res.succeeded()) {
							JsonArray firstBatch = res.result().getJsonObject("cursor").getJsonArray("firstBatch");
							if (firstBatch.size() > 0) {
								JsonObject reply = firstBatch.getJsonObject(0);
								logger.debug("db `findWithCount(aggregate)` {" + op_id + "} succeeded, reply: " + reply);
								hdlr.reply(reply);
							} else {
								JsonObject reply = new JsonObject().put("count", 0).put("docs", new JsonArray()); // send empty result count:0 & docs:[]
								logger.debug("db `findWithCount(aggregate)` {" + op_id + "} succeeded, reply: " + reply);
								hdlr.reply(reply);
							}

						} else {
							logger.error(res.cause(), res.cause());
							hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
						}
					});

				} catch (Exception e) {
					logger.error(e, e);
					hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
				}
			});

			// creation d'un eb consumer pour trouver les données sur l'adresse `db.findOne`
			vertx.eventBus().consumer(Services.DB_FIND_ONE, hdlr -> {
				final String op_id = System.currentTimeMillis() + "-" + Math.round(Math.random() * 100);
				try {
					JsonObject msg = (JsonObject) hdlr.body();
					logger.debug("consumer -service `db.findOne`- {" + op_id + "}, msg: " + msg);

					String collection = msg.getString("collection");
					JsonObject query = msg.getJsonObject("query");
					JsonObject fields = msg.getJsonObject("fields", new JsonObject());

					logger.debug("db `findOne` {" + op_id + "} collection: " + collection + ", query: " + query + ", fields: " + fields);

					client.findOne(collection, query, fields, hdlrFind -> {
						if (hdlrFind.succeeded()) {
							logger.debug("db `findOne` {" + op_id + "} succeeded, reply: " + hdlrFind.result());
							hdlr.reply(hdlrFind.result());
						} else {
							logger.error(hdlrFind.cause(), hdlrFind.cause());
							hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
						}
					});

				} catch (Exception e) {
					logger.error(e, e);
					hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
				}
			});

			// creation d'un eb consumer pour l'insertion des données sur l'adresse `db.insert`
			vertx.eventBus().consumer(Services.DB_INSERT, hdlr -> {
				final String op_id = System.currentTimeMillis() + "-" + Math.round(Math.random() * 100);
				try {
					JsonObject msg = (JsonObject) hdlr.body();
					logger.debug("consumer -service `db.insert`- {" + op_id + "}, msg: " + msg);

					String collection = msg.getString("collection");
					JsonObject query = msg.getJsonObject("query")
							// add timestamp
							.put(Fields.TMSP, System.currentTimeMillis());

					logger.debug("db `insert` {" + op_id + "} collection: " + collection + ", query: " + query);

					client.insert(collection, query, hndlrInsert -> {
						if (hndlrInsert.succeeded()) {
							JsonObject reply = new JsonObject().put("_id", hndlrInsert.result());
							logger.debug("db `insert` {" + op_id + "} succeeded, reply: " + reply);
							hdlr.reply(reply);
						} else {
							logger.error(hndlrInsert.cause(), hndlrInsert.cause());
							hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
						}
					});

				} catch (Exception e) {
					logger.error(e, e);
					hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
				}
			});

			// creation d'un eb consumer pour l'update des données sur l'adresse `db.update`
			vertx.eventBus().consumer(Services.DB_UPDATE, hdlr -> {
				final String op_id = System.currentTimeMillis() + "-" + Math.round(Math.random() * 100);
				try {
					JsonObject msg = (JsonObject) hdlr.body();
					logger.debug("consumer -service `db.update`- {" + op_id + "}, msg: " + msg);

					String collection = msg.getString("collection");
					JsonObject query = msg.getJsonObject("query");
					JsonObject update_ = msg.getJsonObject("update");
					String operator = msg.getString("operator", "$set");

					JsonObject update = new JsonObject().put(operator, update_);
					logger.debug("db `update` {" + op_id + "} collection: " + collection + ", query: " + query + ", update: " + update);

					client.updateCollection(collection, query, update, hdlrUpdate -> {
						if (hdlrUpdate.succeeded()) {
							long count = hdlrUpdate.result().getDocModified();
							JsonObject replay = new JsonObject().put("message", "data updated with success").put("count", count);
							logger.debug("db `update` {" + op_id + "} succeeded, reply: " + replay);
							hdlr.reply(replay);
						} else {
							logger.error(hdlrUpdate.cause(), hdlrUpdate.cause());
							hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
						}
					});

				} catch (Exception e) {
					logger.error(e, e);
					hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
				}
			});

			// creation d'un eb consumer pour l'update des données sur l'adresse `db.updateCollectionWithOptions`
			vertx.eventBus().consumer(Services.DB_UPDATE_COLLECTION_WITH_OPTIONS, hdlr -> {
				try {
					JsonObject msg = (JsonObject) hdlr.body();
					logger.debug("consumer -service `db.update`- , msg: " + msg);

					String collection = msg.getString("collection");
					JsonObject query = msg.getJsonObject("query");
					JsonObject update = msg.getJsonObject("update");
					JsonObject option = msg.getJsonObject("options", new JsonObject());
					UpdateOptions options = new UpdateOptions();
					if (option.containsKey("upsert") && option.getBoolean("upsert"))
						options.setUpsert(true);
					if (option.containsKey("multi") && option.getBoolean("multi"))
						options.setMulti(true);
					client.updateCollectionWithOptions(collection, query, update, options, res -> {
						if (res.succeeded()) {
							logger.debug("db operation `updateCollectionWithOptions` succeeded with  result : " + res.result().toJson());
							long count = res.result().getDocUpsertedId() != null ? 1 : res.result().getDocModified();
							logger.debug("db operation `updateCollectionWithOptions` succeeded with  result ==>  : " + count);
							hdlr.reply(new JsonObject().put("count", count));
						} else {
							logger.error(res.cause(), res.cause());
							hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
						}
					});

				} catch (Exception e) {
					logger.error(e, e);
					hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
				}
			});

			// creation d'un eb consumer pour l'update des données et les retourner sur l'adresse `db.update`
			vertx.eventBus().consumer(Services.DB_UPDATE_ONE_AND_RETURN, hdlr -> {
				try {
					JsonObject msg = (JsonObject) hdlr.body();
					logger.debug("consumer -service `db.updateAndReturn`- , msg: " + msg);

					String collection = msg.getString("collection");
					JsonObject query = msg.getJsonObject("query");
					JsonObject update_ = msg.getJsonObject("update");
					JsonObject option = msg.getJsonObject("options", new JsonObject());
					String operator = msg.getString("operator", "$set");
					JsonObject update = new JsonObject().put(operator, update_);
					FindOptions findOptions = new FindOptions();
					UpdateOptions updateOptions = new UpdateOptions().setReturningNewDocument(true);
					if (option.containsKey("upsert") && option.getBoolean("upsert"))
						updateOptions.setUpsert(true);
					client.findOneAndUpdateWithOptions(collection, query, update, findOptions, updateOptions, hdlrUpdate -> {
						if (hdlrUpdate.succeeded()) {
							logger.debug("db operation `updateAndReturn` succeeded : " + hdlrUpdate.result());
							hdlr.reply(hdlrUpdate.result());
						} else {
							logger.error(hdlrUpdate.cause(), hdlrUpdate.cause());
							hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
						}
					});

				} catch (Exception e) {
					logger.error(e, e);
					hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
				}
			});

			// creation d'un eb consumer pour la suppression d'un document sur l'adresse `db.delete`
			vertx.eventBus().consumer(Services.DB_REMOVE_DOCUMENT, hdlr -> {
				final String op_id = System.currentTimeMillis() + "-" + Math.round(Math.random() * 100);
				try {
					JsonObject msg = (JsonObject) hdlr.body();
					logger.debug("consumer -service `db.delete`- {" + op_id + "}, msg: " + msg);

					String collection = msg.getString("collection");
					JsonObject query = msg.getJsonObject("query");

					logger.debug("db `find` {" + op_id + "} collection: " + collection + ", query: " + query);

					client.removeDocuments(collection, query, hdlrRemoveDocument -> {
						if (hdlrRemoveDocument.succeeded()) {
							JsonObject reply = new JsonObject().put("count_of_deleted", hdlrRemoveDocument.result().getRemovedCount());
							logger.debug("db `delete` {" + op_id + "} succeeded, reply: " + reply);
							hdlr.reply(reply);
						} else {
							logger.error(hdlrRemoveDocument.cause(), hdlrRemoveDocument.cause());
							hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
						}
					});

				} catch (Exception e) {
					logger.error(e, e);
					hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
				}
			});

			// creation d'un eb consumer pour le count des documents d'une collection sur l'adresse `count`
			vertx.eventBus().consumer(Services.DB_COUNT, hdlr -> {
				final String op_id = System.currentTimeMillis() + "-" + Math.round(Math.random() * 100);
				try {
					JsonObject msg = (JsonObject) hdlr.body();
					logger.debug("consumer -service `db.count`- {" + op_id + "}, msg: " + msg);

					String collection = msg.getString("collection");
					JsonObject query = msg.getJsonObject("query");

					logger.debug("db `count` {" + op_id + "} collection: " + collection + ", query: " + query);

					client.count(collection, query, hdlrCount -> {
						if (hdlrCount.succeeded()) {
							JsonObject reply = new JsonObject().put("count", hdlrCount.result());
							logger.debug("db `count` {" + op_id + "} succeeded, reply: " + reply);
							hdlr.reply(reply);
						} else {
							logger.error(hdlrCount.cause(), hdlrCount.cause());
							hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
						}
					});


				} catch (Exception e) {
					logger.error(e, e);
					hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
				}
			});

			//
			vertx.eventBus().consumer(Services.DB_BULK_WRITE, hdlr -> {
				final String op_id = System.currentTimeMillis() + "-" + Math.round(Math.random() * 100);
				try {
					JsonObject msg = (JsonObject) hdlr.body();
					logger.debug("consumer -service `db.bulkWrite`- {" + op_id + "}, msg: " + msg);

					String collection = msg.getString("collection");
					JsonArray query = msg.getJsonArray("query");

					Long timeStamp = msg.getLong(Fields.TMSP, System.currentTimeMillis());

					List<BulkOperation> b = new ArrayList<>();
					for (int i = 0; i < query.size(); i++) {
						b.add(BulkOperation.createInsert(query.getJsonObject(i).put(Fields.TMSP, timeStamp)));
					}

					logger.debug("db `bulkWrite` {" + op_id + "} collection: " + collection + ", query: " + query);

					client.bulkWrite(collection, b, hndlrInsert -> {
						if (hndlrInsert.succeeded()) {
							JsonObject reply = new JsonObject().put("count", hndlrInsert.result().getInsertedCount());
							logger.debug("db `bulkWrite` {" + op_id + "} succeeded, reply: " + reply);
							hdlr.reply(reply);
						} else {
							logger.error(hndlrInsert.cause(), hndlrInsert.cause());
							hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
						}
					});

				} catch (Exception e) {
					logger.error(e, e);
					hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
				}
			});

			// creation d'un eb consumer pour faire l'aggreagation des données sur l'adresse `runCommand`
			vertx.eventBus().consumer(Services.DB_RUN_COMMAND, hdlr -> {
				final String op_id = System.currentTimeMillis() + "-" + Math.round(Math.random() * 100);
				try {
					JsonObject msg = (JsonObject) hdlr.body();
					logger.debug("consumer -service `db.runCommand(aggregate)`- {" + op_id + "}, msg: " + msg);

					String collection = msg.getString("collection");
					// get stages
					JsonArray pipeline = msg.getJsonArray("pipeline");
					// set command
					JsonObject command = new JsonObject()
							.put("aggregate", collection)
							.put("pipeline", pipeline)
							.put("allowDiskUse", true)
							.put("explain", false);

					if (msg.containsKey("batchSize"))
						command.put("cursor", new JsonObject().put("batchSize", msg.getInteger("batchSize")));

					logger.debug("db `runCommand(aggregate)` {" + op_id + "} command: " + command);

					client.runCommand("aggregate", command, res -> {
						if (res.succeeded()) {
							JsonArray reply = res.result().getJsonObject("cursor").getJsonArray("firstBatch");
							logger.debug("db `runCommand(aggregate)` {" + op_id + "} succeeded, reply: " + reply);
							hdlr.reply(reply);
						} else {
							logger.error(res.cause(), res.cause());
							hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
						}
					});

				} catch (Exception e) {
					logger.error(e, e);
					hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
				}
			});

			// creation d'un eb consumer pour retourer des valeur destincts d'un champ `distinct`
			vertx.eventBus().consumer(Services.DB_DISTINCT, hdlr -> {
				final String op_id = System.currentTimeMillis() + "-" + Math.round(Math.random() * 100);
				try {
					JsonObject msg = (JsonObject) hdlr.body();
					logger.debug("consumer -service `db.distinct(collection , fieldName)`- {" + op_id + "}, msg: " + msg);

					String collection = msg.getString("collection");
					String fieldName = msg.getString("fieldName");
					String fieldClassName = msg.getString("fieldClassName", String.class.getName());
					JsonObject query = msg.getJsonObject("query");

					logger.debug("db `distinct(" + collection + " , " + fieldName + ")` {" + op_id + "} ");

					client.distinctWithQuery(collection, fieldName, fieldClassName, query, res -> {
						if (res.succeeded()) {
							JsonArray reply = res.result();
							logger.debug("db `distinct(" + collection + " , " + fieldName + ")` {" + op_id + "} succeeded, reply: " + reply);
							hdlr.reply(reply);
						} else {
							logger.error(res.cause(), res.cause());
							hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
						}
					});

				} catch (Exception e) {
					logger.error(e, e);
					hdlr.fail(Exceptions.TECHNICAL_ERROR.failureCode(), Exceptions.TECHNICAL_ERROR.getMessage());
				}
			});

			startFuture.complete();
		} catch (Exception e) {
			logger.error(e, e);
			startFuture.fail(Exceptions.TECHNICAL_ERROR);
		}
	}
}
