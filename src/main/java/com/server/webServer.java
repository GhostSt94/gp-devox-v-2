package com.server;

import com.utils.constants.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.sstore.SessionStore;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class webServer extends AbstractVerticle {

	private final Logger logger = Logger.getLogger(webServer.class);

	private RouterBuilder routerFactory;
	private MongoAuth authProvider;

	private SessionHandler localSessionHandler;

	private boolean isProduction;
	private int serverPort;
	private int clientPort;
	private String uploadDirectory;

	@Override
	public void start(Promise<Void> startFuture) {
		try {
			// load the application config file
			JsonObject config = config();

			JsonObject db = config.getJsonObject("db");
			isProduction = config.getBoolean("isProduction", true);
			serverPort = config.getInteger("serverPort");
			clientPort = config.getInteger("clientPort");
			uploadDirectory = config.getString("uploadDirectory");


			// init authProvider
			authProvider = MongoAuth.create(MongoClient.createShared(vertx, db), new JsonObject());

			// create localSessionHandler
			localSessionHandler = SessionHandler.create(SessionStore.create(vertx)).setAuthProvider(authProvider);

			// creating the router routerFactory + start httpServer
			RouterBuilder.create(vertx, "openAPI3.json", ar -> {
				try {
					if (ar.failed()) {
						// Something went wrong during router factory initialization
						logger.error(ar.cause(), ar.cause());
						startFuture.fail(ar.cause());
					} else {

						// Spec loaded with success
						routerFactory = ar.result();

						// serve vue application
						StaticHandler vueProjectHandler = StaticHandler.create()
								.setWebRoot("web/dist/")
								.setIndexPage("index.html")
								.setCachingEnabled(isProduction)
								.setDefaultContentEncoding("UTF-8");


						// serve public upload dir
						StaticHandler uploadHandler = StaticHandler.create()
								.setWebRoot(uploadDirectory)
								.setCachingEnabled(isProduction)
								.setDefaultContentEncoding("UTF-8");


						// add handlers

						// Use LocalSessionStore
						routerFactory.operation("GET").handler(localSessionHandler);
						routerFactory.operation("POST").handler(localSessionHandler);
						// (user is logged in) ? add user to routingContext : redirection to login page
						routerFactory.operation("privateGET").handler(this::privateControl);
						routerFactory.operation("privatePOST").handler(this::privateControl);
						routerFactory.operation("GET").handler(vueProjectHandler);
						routerFactory.operation("POST").handler(BodyHandler.create());
						routerFactory.operation("GET").handler(this::globalHandler);
						routerFactory.operation("POST").handler(this::globalHandler);
						routerFactory.operation("uploads").handler(uploadHandler);
						// login + logout
						routerFactory.operation("login").handler(this::loginHandler);
						routerFactory.operation("logout").handler(routingContext -> {
							logger.info("logout");
							if (routingContext.session() != null) {
								logger.debug("destroy session");
								routingContext.session().destroy();
							}
							routingContext.clearUser();
							JsonObject response = new JsonObject()
									.put(Fields.RESPONSE_FLUX_SUCCEEDED, true)
									.put(Fields.RESPONSE_FLUX_DATA, new JsonObject());
							sendResponse(response, routingContext.response());
						});
						routerFactory.operation("getSessionUser").handler(this::getSessionUserHandler);
						routerFactory.operation("isConnected").handler(this::isConnectedHandler);
						// user
						routerFactory.operation("listUser").handler(this::userHandler);
						routerFactory.operation("createUser").handler(this::userHandler);
						routerFactory.operation("updateUser").handler(this::userHandler);
						routerFactory.operation("removeUser").handler(this::userHandler);
						routerFactory.operation("changePassword").handler(this::userHandler);
						routerFactory.operation("generatePassword").handler(this::userHandler);
						routerFactory.operation("resetPassword").handler(this::userHandler);
						routerFactory.operation("newPassword").handler(this::userHandler);

						// Project
						routerFactory.operation("listProject").handler(this::projectHandler);
						routerFactory.operation("createProject").handler(this::projectHandler);
						routerFactory.operation("updateProject").handler(this::projectHandler);
						routerFactory.operation("removeProject").handler(this::projectHandler);
						routerFactory.operation("statsProject").handler(this::projectHandler);

						// Client
						routerFactory.operation("listClient").handler(this::clientHandler);
						routerFactory.operation("createClient").handler(this::clientHandler);
						routerFactory.operation("updateClient").handler(this::clientHandler);
						routerFactory.operation("removeClient").handler(this::clientHandler);

						// Comment
						routerFactory.operation("listComment").handler(this::commentHandler);
						routerFactory.operation("createComment").handler(this::commentHandler);
						routerFactory.operation("removeComment").handler(this::commentHandler);

						// Attachement
						routerFactory.operation("listAttachement").handler(this::attachementHandler);
						routerFactory.operation("createAttachement").handler(this::attachementHandler);
						routerFactory.operation("updateAttachement").handler(this::attachementHandler);
						routerFactory.operation("removeAttachement").handler(this::attachementHandler);
						routerFactory.operation("uploadAttachement").handler(this::uploadAttachement);

						// Facture
						routerFactory.operation("listFacture").handler(this::factureHandler);
						routerFactory.operation("createFacture").handler(this::factureHandler);
						routerFactory.operation("updateFacture").handler(this::factureHandler);
						routerFactory.operation("removeFacture").handler(this::factureHandler);
						routerFactory.operation("statsFacture").handler(this::factureHandler);

						//remove CORS
						if (!isProduction)
							routerFactory.rootHandler(CorsHandler.create("http://localhost:" + clientPort)
									.allowedMethod(HttpMethod.GET)
									.allowedMethod(HttpMethod.POST)
									.allowedMethod(HttpMethod.OPTIONS)
									.allowCredentials(true)
									.allowedHeader("Access-Control-Allow-Method")
									.allowedHeader("Access-Control-Allow-Origin")
									.allowedHeader("Access-Control-Allow-Credentials")
									.allowedHeader("Content-Type")
									.allowedHeader("Content-Encoding"));


						// generate the router
						Router router = routerFactory.createRouter();

						// create the server
						HttpServerOptions httpServerOptions = new HttpServerOptions()
								.setPort(serverPort)
								.setCompressionSupported(true);
						vertx.createHttpServer(httpServerOptions).requestHandler(router).listen(asyncResult -> {
							if (asyncResult.succeeded()) {
								logger.info("Server is listening : http://127.0.0.1:" + serverPort);
								startFuture.complete();
							} else {
								logger.error(asyncResult.cause(), asyncResult.cause());
								startFuture.fail(asyncResult.cause());
							}
						});

					}
				} catch (Exception e) {
					logger.error(e, e);
					startFuture.fail(e);
				}
			});

		} catch (Exception e) {
			logger.error(e, e);
			startFuture.fail(e);
		}
	}

	/**
	 * @param responseContent data of a response
	 * @param response        HttpServerResponse function
	 * @author youssef
	 * <p>
	 * function to send http response to http client
	 * </p>
	 */
	private void sendResponse(JsonObject responseContent, HttpServerResponse response) {
		try {
			logger.debug("sending http Response, content: " + responseContent);
			response.putHeader("content-type", "application/json; charset=utf-8");
			response.end(responseContent.encode());
		} catch (Exception e) {
			logger.error(e, e);
			JsonObject rs = new JsonObject()
					.put(Fields.RESPONSE_FLUX_SUCCEEDED, false)
					.put(Fields.RESPONSE_FLUX_MESSAGE, Exceptions.TECHNICAL_ERROR.failureCode());
			response.end(rs.encode());
		}
	}

	/**
	 * @param routingContext RoutingContext
	 * @author youssef
	 * OpenAPI3 Route globalHandler, a handler added to router before swagger routes, it allow 'cross-origin' on mode DEV
	 */
	private void globalHandler(RoutingContext routingContext) {
		logger.debug("Handler `globalHandler`, path: " + routingContext.request().path() + ", body: " + routingContext.getBody());

		if (routingContext.request().method().toString().equals("GET")) {
			switch (routingContext.request().path()) {
				case "/signup":
				case "/login":
				case "/private":
					routingContext.reroute("/");
					break;
				default:
					routingContext.next();
			}
		} else {
			routingContext.next();
		}
	}

	/**
	 * @param routingContext RoutingContext
	 * @author youssef
	 * <p>
	 * OpenAPI3 Route Handler, a top level route to controle if the current request is coming from a connected user
	 * if true proceed to next handler, else redirect to login page
	 * </p>
	 */
	private void privateControl(RoutingContext routingContext) {
		try {
			logger.debug("Handler `privateControl`");
			// routingContext.next();
			// todo remove comment when session is implemented
			if (routingContext.user() != null) {
				logger.debug("session: " + routingContext.user().principal());
				routingContext.next();
			} else {
				JsonObject resp = new JsonObject()
						.put(Fields.RESPONSE_FLUX_SUCCEEDED, false)
						.put(Fields.RESPONSE_FLUX_MESSAGE, Exceptions.SESSION_EXPIRED.failureCode());
				sendResponse(resp, routingContext.response());
			}

		} catch (Exception e) {
			logger.error(e, e);
			JsonObject resp = new JsonObject()
					.put(Values.RESPONSE_FLUX_SUCCEEDED, false)
					.put("message", Exceptions.TECHNICAL_ERROR.failureCode());
			sendResponse(resp, routingContext.response());
		}
	}

	/**
	 * @param routingContext RoutingContext
	 * @author younes
	 * <p>
	 * OpenAPI3 Route Handler for login ( connection + create session )
	 * request body: { username: "toto@gmail.com", password: "P@ssw0rd" }
	 * </p>
	 */
	private void loginHandler(RoutingContext routingContext) {
		logger.debug("Handler `loginHandler`");
		JsonObject responseContent = new JsonObject();
		HttpServerRequest req = routingContext.request();
		HttpServerResponse response = req.response();
		try {
			JsonObject body = routingContext.getBodyAsJson();
			String username = body.getString("username");
			String password = body.getString("password");

			JsonObject authInfo = new JsonObject().put("username", username).put("password", password);
			authProvider.authenticate(authInfo, asyncResult -> {
				try {
					if (asyncResult.failed()) {
						logger.error(asyncResult.cause(), asyncResult.cause());
						responseContent
								.put(Fields.RESPONSE_FLUX_SUCCEEDED, false)
								.put(Fields.RESPONSE_FLUX_MESSAGE, Exceptions.USER_OR_PASSWORD_INCORRECT.failureCode());
						sendResponse(responseContent, response);
					} else {
						User user = asyncResult.result();
						routingContext.setUser(user);
						JsonObject principalUser = user.principal();
						// remove fields password and salt
						principalUser.remove(Fields.USER_PASSWORD);
						principalUser.remove(Fields.USER_SALT);
						responseContent
								.put(Fields.RESPONSE_FLUX_SUCCEEDED, true)
								.put(Fields.RESPONSE_FLUX_DATA, principalUser);

						sendResponse(responseContent, response);
					}
				} catch (Exception e) {
					logger.error(e, e);
					responseContent
							.put(Fields.RESPONSE_FLUX_SUCCEEDED, false)
							.put(Fields.RESPONSE_FLUX_MESSAGE, Exceptions.TECHNICAL_ERROR.failureCode());
					sendResponse(responseContent, response);
				}
			});
		} catch (Exception e) {
			logger.error(e, e);
			responseContent
					.put(Fields.RESPONSE_FLUX_SUCCEEDED, false)
					.put(Fields.RESPONSE_FLUX_MESSAGE, Exceptions.TECHNICAL_ERROR.failureCode());
			sendResponse(responseContent, response);
		}
	}

	/**
	 * @param routingContext RoutingContext
	 * @author youssef
	 * <p>
	 * OpenAPI3 Route Handler, to get user from session
	 * </p>
	 */
	private void getSessionUserHandler(RoutingContext routingContext) {
		logger.debug("Handler `getSessionUserHandler`");

		HttpServerRequest req = routingContext.request();
		HttpServerResponse resp = req.response();

		JsonObject responseContent = new JsonObject();
		if (routingContext.session() != null && routingContext.user() != null) {
			JsonObject principal = routingContext.user().principal();
			logger.debug("principal: " + principal);
			responseContent
					.put("succeeded", true)
					.put("data", principal);
		} else {
			responseContent
					.put("succeeded", false)
					.put("message", Exceptions.SESSION_EXPIRED.failureCode());
		}
		sendResponse(responseContent, resp);
	}

	private void isConnectedHandler(RoutingContext routingContext) {
		logger.debug("Handler `isConnectedHandler`");

		HttpServerRequest req = routingContext.request();
		HttpServerResponse resp = req.response();

		JsonObject responseContent = new JsonObject();
		if (routingContext.session() != null && routingContext.user() != null) {
			JsonObject principal = routingContext.user().principal();
			logger.debug("principal: " + principal);
			responseContent
					.put("succeeded", true)
					.put("data", true);
		} else {
			responseContent
					.put("succeeded", true)
					.put("data", false);
		}
		sendResponse(responseContent, resp);
	}

	// ------------------		 Project (CRUD)		----------------------
	private void userHandler(RoutingContext routingContext) {
		logger.debug("Handler `userHandler`");
		JsonObject responseContent = new JsonObject();
		HttpServerRequest req = routingContext.request();
		HttpServerResponse response = req.response();
		try {
			String[] levels = routingContext.normalizedPath().split("/");
			String action = levels[levels.length - 1];
			JsonObject body = routingContext.getBodyAsJson();
			logger.debug("body: " + body);
			String serviceName = "";

			switch (action) {
				case "list":
					//body.put("user", routingContext.user().principal());
					serviceName = Services.USER_LIST;
					break;
				case "create":
					body.put("url", routingContext.request().host());
					serviceName = Services.USER_CREATE;
					break;
				case "update":
					serviceName = Services.USER_MODIFY;
					break;
				case "remove":
					serviceName = Services.USER_REMOVE;
					break;
				case "change":
					serviceName = Services.USER_MODIFY_PASSWORD;
					break;
				case "generate":
					serviceName = Services.GENERATE_NEW_PASSWORD;
					break;
				case "reset":
					serviceName = Services.RESET_PASSWORD;
					break;
				case "new":
					serviceName = Services.NEW_PASSWORD;
					break;
			}
			vertx.eventBus().request(serviceName, body, ar -> defaultHandler(ar, routingContext));
		} catch (Exception e) {
			logger.error(e, e);
			responseContent
					.put(Fields.RESPONSE_FLUX_SUCCEEDED, false)
					.put(Fields.RESPONSE_FLUX_MESSAGE, Exceptions.TECHNICAL_ERROR.failureCode());
			sendResponse(responseContent, response);
		}
	}

	// ------------------		 Project (CRUD)		----------------------
	private void projectHandler(RoutingContext routingContext) {
		logger.debug("Handler `projectHandler`");
		JsonObject responseContent = new JsonObject();
		HttpServerRequest req = routingContext.request();
		HttpServerResponse response = req.response();
		try {
			String[] levels = routingContext.normalizedPath().split("/");
			String action = levels[levels.length - 1];
			JsonObject body = routingContext.getBodyAsJson();
			String serviceName = "";
			switch (action) {
				case "list":
					//body.put("user", routingContext.user().principal());
					serviceName = Services.PROJECT_LIST;
					break;
				case "create":
					//body.put("user", routingContext.user().principal());
					serviceName = Services.PROJECT_CREATE;
					break;
				case "update":
					serviceName = Services.PROJECT_MODIFY;
					break;
				case "remove":
					serviceName = Services.PROJECT_REMOVE;
					break;
				case "stats":
					serviceName = Services.PROJECT_STATS;
					break;
			}
			vertx.eventBus().request(serviceName, body, ar -> defaultHandler(ar, routingContext));
		} catch (Exception e) {
			logger.error(e, e);
			responseContent
					.put(Fields.RESPONSE_FLUX_SUCCEEDED, false)
					.put(Fields.RESPONSE_FLUX_MESSAGE, Exceptions.TECHNICAL_ERROR.failureCode());
			sendResponse(responseContent, response);
		}
	}

	// ------------------		 Client (CRUD)		----------------------
	private void clientHandler(RoutingContext routingContext) {
		logger.debug("Handler `clientHandler`");
		JsonObject responseContent = new JsonObject();
		HttpServerRequest req = routingContext.request();
		HttpServerResponse response = req.response();
		try {
			String[] levels = routingContext.normalizedPath().split("/");
			String action = levels[levels.length - 1];
			JsonObject body = routingContext.getBodyAsJson();
			String serviceName = "";
			switch (action) {
				case "list":
					//body.put("user", routingContext.user().principal());
					serviceName = Services.CLIENT_LIST;
					break;
				case "create":
					//body.put("user", routingContext.user().principal());
					serviceName = Services.CLIENT_CREATE;
					break;
				case "update":
					serviceName = Services.CLIENT_MODIFY;
					break;
				case "remove":
					serviceName = Services.CLIENT_REMOVE;
					break;
			}
			vertx.eventBus().request(serviceName, body, ar -> defaultHandler(ar, routingContext));
		} catch (Exception e) {
			logger.error(e, e);
			responseContent
					.put(Fields.RESPONSE_FLUX_SUCCEEDED, false)
					.put(Fields.RESPONSE_FLUX_MESSAGE, Exceptions.TECHNICAL_ERROR.failureCode());
			sendResponse(responseContent, response);
		}
	}

	// ------------------		 Comment (CRUD)		----------------------
	private void commentHandler(RoutingContext routingContext) {
		logger.debug("Handler `commentHandler`");
		JsonObject responseContent = new JsonObject();
		HttpServerRequest req = routingContext.request();
		HttpServerResponse response = req.response();
		try {
			String[] levels = routingContext.normalizedPath().split("/");
			String action = levels[levels.length - 1];
			JsonObject body = routingContext.getBodyAsJson();
			String serviceName = "";
			switch (action) {
				case "list":
					//body.put("user", routingContext.user().principal());
					serviceName = Services.COMMENT_LIST;
					break;
				case "create":
					//body.put("user", routingContext.user().principal());
					serviceName = Services.COMMENT_CREATE;
					break;
				case "remove":
					serviceName = Services.COMMENT_REMOVE;
					break;
			}
			vertx.eventBus().request(serviceName, body, ar -> defaultHandler(ar, routingContext));
		} catch (Exception e) {
			logger.error(e, e);
			responseContent
					.put(Fields.RESPONSE_FLUX_SUCCEEDED, false)
					.put(Fields.RESPONSE_FLUX_MESSAGE, Exceptions.TECHNICAL_ERROR.failureCode());
			sendResponse(responseContent, response);
		}
	}

	// ------------------		 Facture (CRUD)		----------------------
	private void factureHandler(RoutingContext routingContext) {
		logger.debug("Handler `factureHandler`");
		JsonObject responseContent = new JsonObject();
		HttpServerRequest req = routingContext.request();
		HttpServerResponse response = req.response();
		try {
			String[] levels = routingContext.normalizedPath().split("/");
			String action = levels[levels.length - 1];
			JsonObject body = routingContext.getBodyAsJson();
			String serviceName = "";
			switch (action) {
				case "list":
					//body.put("user", routingContext.user().principal());
					serviceName = Services.FACTURE_LIST;
					break;
				case "create":
					//body.put("user", routingContext.user().principal());
					serviceName = Services.FACTURE_CREATE;
					break;
				case "update":
					serviceName = Services.FACTURE_MODIFY;
					break;
				case "remove":
					serviceName = Services.FACTURE_REMOVE;
					break;
				case "stats":
					serviceName = Services.FACTURE_STATS;
					break;
			}
			vertx.eventBus().request(serviceName, body, ar -> defaultHandler(ar, routingContext));
		} catch (Exception e) {
			logger.error(e, e);
			responseContent
					.put(Fields.RESPONSE_FLUX_SUCCEEDED, false)
					.put(Fields.RESPONSE_FLUX_MESSAGE, Exceptions.TECHNICAL_ERROR.failureCode());
			sendResponse(responseContent, response);
		}
	}

	// ------------------		 Attachement (CRUD)		----------------------
	private void attachementHandler(RoutingContext routingContext) {
		logger.debug("Handler `attachementHandler`");
		JsonObject responseContent = new JsonObject();
		HttpServerRequest req = routingContext.request();
		HttpServerResponse response = req.response();
		try {
			String[] levels = routingContext.normalizedPath().split("/");
			String action = levels[levels.length - 1];
			JsonObject body = routingContext.getBodyAsJson();

			//body.put("user", routingContext.user().principal());

			String serviceName = "";
			switch (action) {
				case "list":
					//body.put("user", routingContext.user().principal());
					serviceName = Services.ATTACHEMENT_LIST;
					break;
				case "create":
					//body.put("user", routingContext.user().principal());
					serviceName = Services.ATTACHEMENT_CREATE;
					break;
				case "remove":
					serviceName = Services.ATTACHEMENT_REMOVE;
					break;
			}
			vertx.eventBus().request(serviceName, body, ar -> defaultHandler(ar, routingContext));
		} catch (Exception e) {
			logger.error(e, e);
			responseContent
					.put(Fields.RESPONSE_FLUX_SUCCEEDED, false)
					.put(Fields.RESPONSE_FLUX_MESSAGE, Exceptions.TECHNICAL_ERROR.failureCode());
			sendResponse(responseContent, response);
		}
	}

	private void uploadAttachement(RoutingContext routingContext){
		logger.debug("Handler `uploadAttachement`");
		JsonObject responseContent = new JsonObject();
		HttpServerRequest req = routingContext.request();
		HttpServerResponse response = req.response();
		try{
			Set<FileUpload> fileUploadSet = routingContext.fileUploads();
			Promise<JsonObject> globalPromise = Promise.promise();

			if (fileUploadSet.isEmpty()) {
				globalPromise.fail("empty");
				logger.error("uploads isEmpty or missing params");
				throw Exceptions.TECHNICAL_ERROR;
			} else {

				// prepare request params
				MultiMap params = routingContext.request().params();
				Map<String, Object> map = new HashMap<>();
				for (String name : params.names()) {
					map.put(name, params.get(name));
				}

				// prepare uploaded files
				Iterator<FileUpload> fileUploadIterator = fileUploadSet.iterator();
				JsonArray uploadedFiles = new JsonArray();
				while (fileUploadIterator.hasNext()) {
					FileUpload fileUpload = fileUploadIterator.next();
					JsonObject uploadedFile = new JsonObject()
							.put("path", fileUpload.uploadedFileName())
							.put("name", fileUpload.fileName());
					uploadedFiles.add(uploadedFile);
				}

				JsonObject msg = new JsonObject()
						.put("params", new JsonObject(map))
						.put("uploadedFiles", uploadedFiles);

				vertx.eventBus().request(Services.UPLOAD,msg,rep->{
					if(rep.succeeded()) {

						System.out.println(rep.result().body());
						JsonObject obj=new JsonObject()
								.put("file",rep.result().body());

						if(map.containsKey("id_project"))  obj.put("id_project",map.get("id_project"));
						if(map.containsKey("id_facture"))  obj.put("id_facture",map.get("id_facture"));

						globalPromise.complete(obj);

					}else {
						globalPromise.fail(rep.cause());
					}
				});
				globalPromise.future().compose(attachments-> {
					Promise<JsonObject> promise = Promise.promise();

					try {
						vertx.eventBus().request(Services.ATTACHEMENT_CREATE,attachments, hdlr ->{

							if(hdlr.succeeded()){
								promise.complete((JsonObject) hdlr.result().body());
							}else {
								logger.error(hdlr.cause(),hdlr.cause());
								promise.fail(Exceptions.TECHNICAL_ERROR);
							}

						});
					} catch (Exception e) {
						logger.error(e,e);
						promise.fail(Exceptions.TECHNICAL_ERROR);
					}

					return promise.future();

				}).onComplete(ar->{

					if (ar.failed()) {
						logger.error(ar.cause(), ar.cause());
						responseContent
								.put(Fields.RESPONSE_FLUX_SUCCEEDED, false)
								.put(Fields.RESPONSE_FLUX_MESSAGE, ((ReplyException) ar.cause()).failureCode());
					} else {
						responseContent
								.put(Fields.RESPONSE_FLUX_SUCCEEDED, true)
								.put(Fields.RESPONSE_FLUX_DATA, ar.result());
						sendResponse(responseContent, response);
					}
				});
			}

		}catch(Exception e){
			logger.error(e,e);
			responseContent
					.put(Fields.RESPONSE_FLUX_SUCCEEDED, false)
					.put(Fields.RESPONSE_FLUX_MESSAGE, Exceptions.TECHNICAL_ERROR.failureCode());
			sendResponse(responseContent, response);
		}
	}

	// ------------------  eb.send(), defaultHandler  ----------------------
	private void defaultHandler(AsyncResult<Message<Object>> ar, RoutingContext routingContext) {
		HttpServerRequest req = routingContext.request();
		JsonObject responseContent = new JsonObject();
		HttpServerResponse response = req.response();
		if (ar.failed()) {
			logger.error(ar.cause(), ar.cause());
			responseContent
					.put(Fields.RESPONSE_FLUX_SUCCEEDED, false)
					.put(Fields.RESPONSE_FLUX_MESSAGE, ((ReplyException) ar.cause()).failureCode());
		} else {
			responseContent
					.put(Fields.RESPONSE_FLUX_SUCCEEDED, true)
					.put(Fields.RESPONSE_FLUX_DATA, ar.result().body());
		}
		sendResponse(responseContent, response);
	}

}
