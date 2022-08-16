package com.services;

import com.utils.StaticFunctions;
import com.utils.constants.Exceptions;
import com.utils.constants.Fields;
import com.utils.constants.Services;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author youssef
 */
public class Upload extends AbstractVerticle {

	private static final Logger logger = Logger.getLogger(Upload.class);

	// api to access uploaded files
	private final String SERVED_STATIC_RESOURCES = "/upload/";
	// default path dir for uploads
	private String uploadDir = "uploads";

	@Override
	public void start(Promise<Void> startPromise) {
		try {
			vertx.eventBus().consumer(Services.UPLOAD, this::upload);
			vertx.eventBus().consumer(Services.REMOVE_UPLOAD, this::removeFile);
			uploadDir = config().getString("uploadDirectory");
			if (!StaticFunctions.prepareDirectory(uploadDir)) {
				startPromise.fail(Exceptions.TECHNICAL_ERROR);
			} else {
				startPromise.complete();
			}
		} catch (Exception e) {
			logger.error(e, e);
			startPromise.fail(e);
		}
	}

	private void upload(Message<JsonObject> message) {
		try {
			logger.debug("consumer -service `" + Services.UPLOAD + "` -");

			JsonObject body = message.body();

			JsonArray uploadedFiles = body.getJsonArray("uploadedFiles");
			JsonObject params = body.getJsonObject("params");
			// TODO: remove comment for multiple uploads
			//List<Future> futureList = new ArrayList<>();

			//for (int i = 0; i < uploadedFiles.size(); i++) {

			Promise<JsonObject> promise = Promise.promise();
			//futureList.add(promise.future());

			JsonObject uploadedFile = uploadedFiles.getJsonObject(0);

			String path = uploadedFile.getString("path");
			String name = uploadedFile.getString("name");

			// save file
			moveFileToStaticDir(uploadDir, path, name).future().onComplete(ar -> {
				if(ar.failed()) {
					promise.fail(ar.cause());
				} else {
					JsonObject result = ar.result();
					JsonObject attachedFile = new JsonObject()
							.put(Fields.ATTACHMENT_NAME, name)
							.put(Fields.ATTACHMENT_PATH, result.getString("filePath"))
							.put(Fields.ATTACHMENT_LINK, SERVED_STATIC_RESOURCES + result.getString("name"));
					promise.complete(attachedFile);
				}
			});
			//}

			//CompositeFuture.all(futureList)
			promise.future().onComplete(ar -> {
				if (ar.failed()) {
					logger.error(ar.cause(), ar.cause());
					message.fail(((ReplyException) ar.cause()).failureCode(), ar.cause().getMessage());
				} else {
                        /*JsonArray result = new JsonArray();
                        // get futures result
                        for (Future future : futureList) {
                            result.add(future.result());
                        }*/
					JsonObject result = ar.result();
					message.reply(result);
				}
			});

		} catch (Exception e) {
			logger.error(e, e);
			message.fail(Exceptions.TECHNICAL_ERROR.failureCode(),
					Exceptions.TECHNICAL_ERROR.getMessage());
		}
	}

	public Promise<JsonObject> moveFileToStaticDir(String dest, String path, String name) {
		logger.debug("moveFileToStaticDir...");
		Promise<JsonObject> promise = Promise.promise();
		try {
			FileSystem fs = vertx.fileSystem();
			vertx.<JsonObject>executeBlocking(blockingPromise -> {
				// get name for file
				getFileName(dest, name, 0).future()
						// move file to a specific path
						.compose(v -> {
							Promise<JsonObject> movePromise = Promise.promise();
							fs.move(path, v.getString("filePath"), res -> {
								if (res.succeeded()) {
									movePromise.complete(v);
								} else {
									movePromise.fail(res.cause());
								}
							});
							return movePromise.future();
						})
						.onComplete(ar -> {
							if (ar.failed()) {
								blockingPromise.fail(ar.cause());
							} else {
								blockingPromise.complete(ar.result());
							}
						});
			}).onComplete(ar -> {
				if (ar.failed())
					promise.fail(ar.cause());
				else
					promise.complete(ar.result());
			});
		} catch (Exception e) {
			logger.error(e, e);
			promise.fail(Exceptions.TECHNICAL_ERROR);
		}
		return promise;
	}

	private Promise<JsonObject> getFileName(String dest, String fileName, int num) {
		logger.debug("getFileName...");
		Promise<JsonObject> promise = Promise.promise();
		try {
			// get file name
			String name = fileName.substring(0, fileName.lastIndexOf("."));
			// get file extension
			String[] element = fileName.split("\\.");
			String extension = element[element.length - 1];
			String dateTime = StaticFunctions.formatDate(System.currentTimeMillis(), "dd-MM-yyyy_hh-mm");
			String newName = name + "_" + dateTime + (num != 0 ? "_" + num : "") + "." + extension;
			// prepare destination path
			String filePath = dest + "/" + newName;
			FileSystem fs = vertx.fileSystem();
			fs.exists(filePath, ex -> {
				if (ex.succeeded()) {
					if (ex.result()) {
						getFileName(dest, fileName, num + 1).future().onComplete(ar -> {
							promise.complete(ar.result());
						});
					} else {
						promise.complete(new JsonObject()
								.put("filePath", filePath)
								.put("name", newName)
						);
					}
				} else {
					logger.error(ex.cause(), ex.cause());
					promise.fail(Exceptions.TECHNICAL_ERROR);
				}
			});

		} catch (Exception e) {
			logger.error(e, e);
			promise.fail(Exceptions.TECHNICAL_ERROR);
		}
		return promise;
	}

	private void removeFile(Message<JsonObject> message){
		logger.debug("removeFile...");

		String path = message.body().getString("path");
		try {
			FileSystem fs = vertx.fileSystem();

			fs.delete(path, ex -> {
				if (ex.succeeded()) {
					logger.debug("File deleted from directory");
					message.reply(true);
				} else {
					logger.error(ex.cause(), ex.cause());
					message.fail(0,ex.cause().getMessage());
				}
			});

		} catch (Exception e) {
			logger.error(e, e);
			message.fail(0,Exceptions.TECHNICAL_ERROR.getMessage());
		}
	}

}

