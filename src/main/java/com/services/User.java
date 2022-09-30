package com.services;

import io.vertx.core.Future;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import com.utils.constants.Collections;
import com.utils.constants.Exceptions;
import com.utils.constants.Fields;
import com.utils.constants.Services;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.VertxException;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import org.apache.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class User extends AbstractVerticle {

    private final Logger logger = Logger.getLogger(User.class);
    private MongoAuth authProvider;

    @Override
    public void start(Promise<Void> startPromise) {
        try {
            //config mongo db
            JsonObject mongoconfig = config().getJsonObject("db");

            // creer un clien mongoDB
            MongoClient client = MongoClient.createShared(vertx, mongoconfig);

            JsonObject authProperties = new JsonObject();
            authProvider = MongoAuth.create(client, authProperties);

            // getUserList service
            vertx.eventBus().consumer(Services.USER_LIST, this::getListUsersHandler);

            // createUser service
            vertx.eventBus().consumer(Services.USER_CREATE, this::createUserHandler);

            // modifyUser service
            vertx.eventBus().consumer(Services.USER_MODIFY, this::modifyUserHandler);

            //removeUser service
            vertx.eventBus().consumer(Services.USER_REMOVE, this::removeUserHandler);

            //modifyUserPassword
            vertx.eventBus().consumer(Services.USER_MODIFY_PASSWORD, this::changeUserPassHandler);

            //generate new Password
            vertx.eventBus().consumer(Services.GENERATE_NEW_PASSWORD, this::sendPassword);

            //forget Password
            vertx.eventBus().consumer(Services.RESET_PASSWORD, this::resetPassword);

            //new Password
            vertx.eventBus().consumer(Services.NEW_PASSWORD, this::newPassword);

            startPromise.complete();
        } catch (Exception e) {
            logger.error(e, e);
            startPromise.fail(e);
        }
    }

    /**
     * @param hdlrListUsers Message
     * @author : Oussama
     * <p>
     * this function is an event bus consumer handler that return a list of users
     * respecting a query and options and return a JsonObject that contains count and docs of users(without passwords)
     * </p>
     */
    private void getListUsersHandler(Message hdlrListUsers) {
        try {
            // extract message body
            JsonObject body = (JsonObject) hdlrListUsers.body();
            logger.debug("consumer -service `" + Services.USER_LIST + "`- , msg: " + body);

            JsonObject body_query= body.getJsonObject("query", new JsonObject());
            JsonObject body_options= body_query.getJsonObject("options", new JsonObject());

            // prepare message add projection to find_with_db
            JsonObject fields = new JsonObject()
                    .put(Fields.USER_PASSWORD, 0) // not return password field in the result
                    .put(Fields.USER_SALT, 0); // not return field salt in the result

            JsonObject db_message = new JsonObject()
                    .put("collection", Collections.USER)
                    .put("query", body_query)
                    .put("options", body_options.put("fields",fields));

            vertx.eventBus().request(Services.DB_FIND_WITH_COUNT, db_message, hdlr -> {
                if (hdlr.failed()) {
                    logger.error(hdlr.cause(), hdlr.cause());
                    hdlrListUsers.fail(((ReplyException) hdlr.cause()).failureCode(), hdlr.cause().getMessage());
                } else {
                    logger.debug("get List Users consumer find result : " + hdlr.result().body());
                    hdlrListUsers.reply((JsonObject) hdlr.result().body());
                }
            });


        } catch (Exception e) {
            logger.error(e, e);
            hdlrListUsers.fail(Exceptions.TECHNICAL_ERROR.failureCode(),
                    Exceptions.TECHNICAL_ERROR.getMessage());
        }
    }

    /**
     * @param hdlrCreateUser : Message
     * @author Oussama
     * <p>
     * This service create user account based on authentication provider
     * from MongoDB, return JsonObject containing the created user object
     * </p>
     */
    private void createUserHandler(Message hdlrCreateUser) {
        try {
            JsonObject body = (JsonObject) hdlrCreateUser.body();
            logger.debug("message: " + body);

            // extract fields from message body
            //String password = body.getString(Fields.USER_PASSWORD);
            String username = body.getString(Fields.USER_USERNAME);
            String password = generatePassword();
//            List roles = body.getJsonArray(Fields.USER_ROLE).getList();
//            List permissions = body.getJsonArray(Fields.USER_PERMISSIONS, new JsonArray()).getList();

            JsonObject mail_data= new JsonObject()
                    .put("to",body.getString("email"))
                    .put("username",username)
                    .put("password", password)
                    .put("url", body.getString("url"));

            //Promise<String> insertUserPromise = Promise.promise();
            Future.<String>future(promise ->{

                authProvider.insertUser(username, password, new ArrayList<>(), new ArrayList<>(), res -> {
                    if (res.succeeded()) {
                        String _id = res.result();
                        logger.debug("authProvider.insertUser done successfully, _id: " + _id);
                        promise.complete(_id);
                    } else {
                        if (res.cause() instanceof com.mongodb.MongoWriteException) {
                            logger.debug(res.cause().getMessage());
                            promise.fail(Exceptions.DUPLICATED_USERNAME);
                        } else {
                            logger.error(res.cause(), res.cause());
                            promise.fail(Exceptions.TECHNICAL_ERROR);
                        }
                    }
                });
            }).compose(id->{

                Promise<String> updateUser = Promise.promise();
                String _id = id;
                JsonObject updateMessage = body.copy();
                updateMessage.put("_id", _id);
                updateMessage.put(Fields.USER_DATE_CREATION, System.currentTimeMillis());

                // dont update username, password, roles, permissions
                updateMessage.remove(Fields.USER_USERNAME);
                updateMessage.remove(Fields.USER_PASSWORD);

                vertx.eventBus().request(Services.USER_MODIFY, updateMessage, response -> {
                    if (response.succeeded()) {
                        logger.debug("User status is updated " + response.result().body());
                        updateUser.complete(id);
                    } else {
                        logger.error(response.cause(), response.cause());
                        updateUser.fail(Exceptions.TECHNICAL_ERROR);
                    }
                });
                return updateUser.future();

            }).onComplete(h -> {
                if (h.failed()) {
                    logger.debug("failed insert user");
                    hdlrCreateUser.fail(((ReplyException) h.cause()).failureCode(), h.cause().getMessage());
                } else {
                    sendMail(mail_data);

                    body.put("email",h.result());
                    body.remove(Fields.USER_PASSWORD);
                    body.put("_id", h.result());
                    logger.debug("replay: " + body);
                    hdlrCreateUser.reply(body);
                }
            });
            // insert user (username, password, roles, permissions) with auth provider
            /*authProvider.insertUser(username, password, new ArrayList<>(), new ArrayList<>(), res -> {
                if (res.succeeded()) {
                    String _id = res.result();
                    logger.debug("authProvider.insertUser done successfully, _id: " + _id);
                    insertUserPromise.complete(_id);
                } else {
                    if (res.cause() instanceof com.mongodb.MongoWriteException) {
                        logger.debug(res.cause().getMessage());
                        insertUserPromise.fail(Exceptions.DUPLICATED_USERNAME);
                    } else {
                        logger.error(res.cause(), res.cause());
                        insertUserPromise.fail(Exceptions.TECHNICAL_ERROR);
                    }
                }
            });*/

            // add rest of fields to doc user
            /*insertUserPromise.future()
                    .compose(v -> {
                        Promise<String> promise = Promise.promise();
                        String _id = v;
                        JsonObject updateMessage = body.copy();
                        updateMessage.put("_id", _id);
                        updateMessage.put(Fields.USER_DATE_CREATION, System.currentTimeMillis());

                        // dont update username, password, roles, permissions
                        updateMessage.remove(Fields.USER_USERNAME);
                        updateMessage.remove(Fields.USER_PASSWORD);
//                        updateMessage.remove(Fields.USER_ROLE);
//                        updateMessage.remove(Fields.USER_PERMISSIONS);

                        vertx.eventBus().request(Services.USER_MODIFY, updateMessage, response -> {
                            if (response.succeeded()) {
                                logger.debug("User status is updated " + response.result().body());
                                promise.complete(v);
                            } else {
                                logger.error(response.cause(), response.cause());
                                promise.fail(Exceptions.TECHNICAL_ERROR);
                            }
                        });

                        return promise.future();
                    })
                    .onComplete(h -> {
                        if (h.failed()) {
                            hdlrCreateUser.fail(((ReplyException) h.cause()).failureCode(), h.cause().getMessage());
                        } else {
                            sendMail.onComplete(ar->{
                                body.put("email",ar.result());
                                body.remove(Fields.USER_PASSWORD);
                                body.put("_id", h.result());
                                logger.debug("replay: " + body);
                                hdlrCreateUser.reply(body);
                            });
                        }
                    });*/

        } catch (Exception e) {
            logger.error(e, e);
            hdlrCreateUser.fail(Exceptions.TECHNICAL_ERROR.failureCode(),
                    Exceptions.TECHNICAL_ERROR.getMessage());
        }

    }

    /**
     * @param hdlrModifyUser Message
     * @author Younes, Oussama
     * <p>
     * This service modify a user
     * from MongoDB, return JsonObject containing _id
     * </p>
     */
    private void modifyUserHandler(Message hdlrModifyUser) {
        try {
            JsonObject body = (JsonObject) hdlrModifyUser.body();
            logger.debug("message: " + body);

            String _id = body.getString("_id");

            String collection = Collections.USER;
            if(body.containsKey("collection")){
                collection = (String) body.remove("collection");
            }

            Promise<String> preparePasswordPromise = Promise.promise();
            if (body.containsKey(Fields.USER_PASSWORD)) {
                preparePasswordPromise = preparePassword(collection, _id, body.getString(Fields.USER_PASSWORD));
            } else {
                preparePasswordPromise.complete(null);
            }

            String finalCollection = collection;
            preparePasswordPromise.future()
                    .compose(v -> {
                        Promise<JsonObject> promise = Promise.promise();

                        if (v != null)
                            body.put(Fields.USER_PASSWORD, v);

                        JsonObject query = new JsonObject()
                                .put("_id", body.remove("_id"));

                        if (!query.isEmpty()) {

                            JsonObject updateMessage = new JsonObject()
                                    .put("collection", finalCollection)
                                    .put("query", query)
                                    .put("update", body);

                            vertx.eventBus().request(Services.DB_UPDATE, updateMessage, res -> {
                                if (res.failed()) {
                                    logger.error(res.cause(), res.cause());
                                    promise.fail(res.cause());
                                } else {
                                    promise.complete((JsonObject) res.result().body());
                                }
                            });

                        } else {
                            logger.info("Nothing to update");
                            promise.complete();
                        }

                        return promise.future();
                    })
                    .onComplete(ar -> {
                        if (ar.failed()) {
                            hdlrModifyUser.fail(((ReplyException) ar.cause()).failureCode(), ar.cause().getMessage());
                        } else {
                            JsonObject result = ar.result();
                            logger.debug("replay: "+result);
                            hdlrModifyUser.reply(result);
                        }
                    });


        } catch (Exception e) {
            logger.error(e, e);
            hdlrModifyUser.fail(Exceptions.TECHNICAL_ERROR.failureCode(),
                    Exceptions.TECHNICAL_ERROR.getMessage());
        }
    }

    /**
     * @param hdlrModifyUser Message
     * @author Oussama
     * <p>
     * This service deletes user account with the given _id
     * </p>
     */
    private void removeUserHandler(Message hdlrModifyUser) {
        try {
            // create msg
            JsonObject user = (JsonObject) hdlrModifyUser.body();
            logger.debug("consumer -service `" + Services.USER_REMOVE + "`- , msg: " + user);

            // extract id from message body
            String user_id = user.getString("_id");

            // prepare message for event bus
            JsonObject msg = new JsonObject()
                    .put("collection", Collections.USER)
                    .put("query", new JsonObject()
                            .put("_id", user_id));

            // delete user
            vertx.eventBus().request(Services.DB_REMOVE_DOCUMENT, msg, res -> {
                if (res.failed()) {
                    logger.error(res.cause(), res.cause());
                    hdlrModifyUser.fail(((ReplyException) res.cause()).failureCode(), res.cause().getMessage());
                } else {
                    hdlrModifyUser.reply(res.result().body());
                }
            });

        } catch (Exception e) {
            logger.error(e, e);
            hdlrModifyUser.fail(Exceptions.TECHNICAL_ERROR.failureCode(),
                    Exceptions.TECHNICAL_ERROR.getMessage());
        }
    }

    private void sendPassword(Message hdlrModifyUser) {
        try {
            // create msg
            JsonObject body = (JsonObject) hdlrModifyUser.body();
            JsonObject user = body.getJsonObject("user");
            logger.debug("consumer -service `" + Services.GENERATE_NEW_PASSWORD + "`- , msg: " + user);

            // extract id from message body
            String user_id = user.getString("_id");

            String password = generatePassword();

            preparePassword(Collections.USER, user_id, password).future()
                    .onComplete( ar-> {

                        JsonObject msg = new JsonObject()
                                .put("collection", Collections.USER)
                                .put("query", new JsonObject()
                                        .put("_id", user_id))
                                .put("update", new JsonObject()
                                        .put(Fields.USER_PASSWORD, ar.result()));

                        vertx.eventBus().request(Services.DB_UPDATE, msg, res->{
                            if (res.failed()) {
                                logger.error(res.cause(), res.cause());
                                hdlrModifyUser.fail(((ReplyException) res.cause()).failureCode(), res.cause().getMessage());
                            } else {
                                JsonObject mail_data = new JsonObject().put("to",user.getString("email"))
                                        .put("password", password);
                                sendMail(mail_data);
                                hdlrModifyUser.reply(res.result().body());
                            }
                        });
                    });

        } catch (Exception e) {
            logger.error(e, e);
            hdlrModifyUser.fail(Exceptions.TECHNICAL_ERROR.failureCode(),
                    Exceptions.TECHNICAL_ERROR.getMessage());
        }
    }

    /**
     * @param message Message
     * @author Reda
     * <p>
     * take username, url (from window.origin + /user/password/reset)
     * check if user exists
     * generate a resetPin and hashe it
     * create doc in collection reset_password with (userId, resetPin, expireAt)
     * create link (/user/password/reset/{userId}/{resetPin})
     * send mail with link
     * </p>
     */
    private void resetPassword(Message message) {
        try {
            JsonObject body = (JsonObject) message.body();
            logger.debug("message: " + body);

            String username = body.getString("username");
            String url = body.getString("url");

            //check if user exists
            Promise<JsonObject> findUser = Promise.promise();
            JsonObject msg = new JsonObject()
                    .put("collection", Collections.USER)
                    .put("query", new JsonObject().put("username", username));

            vertx.eventBus().request(Services.DB_FIND_ONE, msg, ar -> {
                if (ar.failed()) {
                    logger.error(ar.cause(), ar.cause());
                    findUser.fail(ar.cause());
                } else {
                    JsonObject resultat = (JsonObject) ar.result().body();
                    if (resultat == null) {
                        message.fail(Exceptions.USER_OR_PASSWORD_INCORRECT.failureCode(), Exceptions.USER_OR_PASSWORD_INCORRECT.getMessage());
                    } else {
                        findUser.complete(resultat);
                    }
                }
            });
            findUser.future().compose(user -> {
                //generate resetPin & and insert to reset_password coll
                Promise<JsonObject> createResetPin = Promise.promise();
                String resetPin = computeHash(generatePassword(6), user.getString("salt"));

                JsonObject data = new JsonObject()
                        .put("userId", user.getString("_id"))
                        .put("resetPin", resetPin)
                        .put("expireAt", System.currentTimeMillis() + (30 * 60 * 1000));

                JsonObject query = new JsonObject()
                        .put("collection", Collections.RESET_PASSWORD)
                        .put("query", data);

                JsonObject all_data = new JsonObject()
                        .put("user", user)
                        .put("resetInfo", data);

                vertx.eventBus().<JsonObject>request(Services.DB_INSERT, query, ar -> {

                    if (ar.failed()) {
                        logger.error(ar.cause(), ar.cause());
                        createResetPin.fail(ar.cause().getMessage());
                    } else {
                        JsonObject result = ar.result().body();
                        all_data.put("resetPwd", result);
                        createResetPin.complete(all_data);
                    }
                });

                return createResetPin.future();

            }).onComplete(ar -> {
                JsonObject reset_info = ar.result().getJsonObject("resetInfo");
                JsonObject user = ar.result().getJsonObject("user");

                //send mail with link

                String link = url + reset_info.getString("userId") + "/" + reset_info.getString("resetPin");
                String mail_body = "<p>Voici le lien pour rèinitialiser votre mot de passe <a target='_blank' href=" + link + ">consulter lien</a>.</p><p>Ce lient est valid pour 30 min.</p>";
                JsonObject mail_data = new JsonObject()
                        .put("to", user.getString("email"))
                        .put("body", mail_body)
                        .put("subject", "Rèinitialisation du mot de passe");
                sendMail(mail_data);

                message.reply("mail sent");
            });

        } catch (Exception e) {
            logger.error(e, e);
            message.fail(Exceptions.TECHNICAL_ERROR.failureCode(),
                    Exceptions.TECHNICAL_ERROR.getMessage());
        }
    }

    /**
     * @param message Message
     * @author Reda
     * <p>
     * new Password after reset with one time link
     * check if user exists with user Id
     * hash the given password / update password in DB
     * remove reset_password doc from DB with userId
     * </p>
     */
    private void newPassword(Message message) {
        try {
            JsonObject body = (JsonObject) message.body();
            logger.debug("message: " + body);

            String user_id = body.getString("userId");
            String reset_pin = body.getString("resetPin");
            String password = body.getString("password");

            Promise<JsonObject> findUser = Promise.promise();
            JsonObject msg = new JsonObject()
                    .put("collection", Collections.RESET_PASSWORD)
                    .put("query", new JsonObject()
                            .put("userId", user_id)
                            .put("resetPin", reset_pin)
                    );

            vertx.eventBus().request(Services.DB_FIND_ONE, msg, ar -> {
                if (ar.failed()) {
                    logger.error(ar.cause(), ar.cause());
                    findUser.fail(ar.cause());
                } else {
                    JsonObject resultat = (JsonObject) ar.result().body();
                    if (resultat == null) {
                        findUser.fail(Exceptions.TECHNICAL_ERROR);
                    } else {
                        findUser.complete(resultat);
                    }
                }
            });
            findUser.future().compose(resetInfo -> {
                Promise<String> hashPassword = Promise.promise();

                try {
                    if(System.currentTimeMillis() > resetInfo.getLong("expireAt")){
                        logger.error("Reset password link expired");
                        message.fail(Exceptions.RESET_PASSWORD_LINK_EXPIRED.failureCode(), Exceptions.RESET_PASSWORD_LINK_EXPIRED.getMessage());
                    }else{
                        JsonObject query = new JsonObject()
                                .put("collection", Collections.USER)
                                .put("query", new JsonObject().put("_id", user_id));

                        vertx.eventBus().<JsonObject>request(Services.DB_FIND_ONE, query, ar -> {
                            if (ar.failed()) {
                                logger.error(ar.cause(), ar.cause());
                                hashPassword.fail(ar.cause().getMessage());
                            } else {
                                JsonObject user = ar.result().body();
                                String hashed_pwd = computeHash(password, user.getString("salt"));

                                hashPassword.complete(hashed_pwd);
                            }
                        });
                    }

                } catch (Exception e) {
                    logger.error(e, e);
                    message.fail(Exceptions.TECHNICAL_ERROR.failureCode(),
                            Exceptions.TECHNICAL_ERROR.getMessage());
                }

                return hashPassword.future();

            }).compose(pwd -> {
                Promise updatePassword = Promise.promise();

                JsonObject data =  new JsonObject()
                        .put("password", pwd);

                JsonObject query = new JsonObject()
                        .put("collection", Collections.USER)
                        .put("query", new JsonObject().put("_id", user_id))
                        .put("update", data);

                vertx.eventBus().request(Services.DB_UPDATE, query, ar -> {
                    if (ar.failed()) {
                        logger.error(ar.cause(), ar.cause());
                        updatePassword.fail(ar.cause().getMessage());
                    } else {
                        updatePassword.complete();
                    }
                });

                return updatePassword.future();
            }).onComplete(ar->{
                JsonObject query = new JsonObject()
                        .put("collection", Collections.RESET_PASSWORD)
                        .put("query", new JsonObject()
                                .put("userId", user_id));

                vertx.eventBus().request(Services.DB_REMOVE_DOCUMENT, query, ar2 -> {
                    if (ar2.failed()) {
                        logger.error(ar2.cause(), ar2.cause());
                        message.fail(Exceptions.TECHNICAL_ERROR.failureCode(),
                                Exceptions.TECHNICAL_ERROR.getMessage());
                    } else {
                        message.reply("Password modifié");
                    }
                });
            });

        } catch (Exception e) {
            logger.error(e, e);
            message.fail(Exceptions.TECHNICAL_ERROR.failureCode(),
                    Exceptions.TECHNICAL_ERROR.getMessage());
        }
    }
    /**
     * @param user_id  String
     * @param password String
     * @return Future<String> hashedPassword
     * <p>
     * hash password with the user salt from database
     * </p>
     */
    private Promise<String> preparePassword(String collection, String user_id, String password) {
        Promise<String> promise = Promise.promise();
        try {
            // get salt from database
            JsonObject query = new JsonObject()
                    .put("collection", collection)
                    .put("query", new JsonObject().put("_id", user_id));

            vertx.eventBus().request(Services.DB_FIND_ONE, query, hdlr -> {
                if (hdlr.failed()) {
                    logger.error(hdlr.cause(), hdlr.cause());
                    promise.fail(Exceptions.TECHNICAL_ERROR);
                } else {
                    JsonObject result = (JsonObject) hdlr.result().body();
                    if (result == null) {
                        logger.error("user not found");
                        promise.fail(Exceptions.TECHNICAL_ERROR);
                    }else {
                        String hashedPassword = computeHash(password, result.getString(Fields.USER_SALT));
                        promise.complete(hashedPassword);
                    }
                }
            });
        } catch (Exception e) {
            logger.error(e, e);
            promise.fail(Exceptions.TECHNICAL_ERROR);
        }
        return promise;
    }

    /**
     * @param hdlrModifyUser Message
     * @author Oussama
     * <p>
     * This service modifies personal password of the connected user(ADHERENT)
     * </p>
     */
    private void changeUserPassHandler(Message hdlrModifyUser) {
        try {
            JsonObject body = (JsonObject) hdlrModifyUser.body();
            logger.debug("consumer -service `" + Services.USER_MODIFY_PASSWORD + "`- , msg: " + body);

            String user_id = body.getString("user_id");
            String old_password = body.getString("oldPassword");
            String new_pass = body.getString("newPassword");

            Promise<JsonObject> valide_user_future = Promise.promise();

            // find/get new version of user
            JsonObject query = new JsonObject()
                    .put("collection", Collections.USER)
                    .put("query", new JsonObject().put("_id", user_id));

            vertx.eventBus().request(Services.DB_FIND_ONE, query, hdlr -> {
                if (hdlr.failed()) {
                    logger.error(hdlr.cause(), hdlr.cause());
                    valide_user_future.fail(Exceptions.TECHNICAL_ERROR);
                } else {
                    JsonObject result = (JsonObject) hdlr.result().body();
                    if (result == null){
                        logger.error("user not found");
                        valide_user_future.fail(Exceptions.TECHNICAL_ERROR);
                    }else {
                        valide_user_future.complete(result);
                    }
                }
            });

            valide_user_future.future().compose(user->{
                Promise<JsonObject> promise = Promise.promise();
                String old_hashedPassword = computeHash(old_password, user.getString(Fields.USER_SALT));
                //compare with users current password
                if(old_hashedPassword.equals(user.getString(Fields.USER_PASSWORD))){
                    promise.complete(user);
                }
                else{
                    promise.fail(Exceptions.WRONG_PASSWORD);
                }
                return promise.future();
            }).compose(user ->{
                Promise<JsonObject> promise = Promise.promise();
                String new_hashedPassword = computeHash(new_pass, user.getString(Fields.USER_SALT));
                //update user

                JsonObject updateMessage = new JsonObject()
                        .put("collection", Collections.USER)
                        .put("query", new JsonObject()
                                .put("_id", user_id))
                        .put("update", new JsonObject().put(Fields.USER_PASSWORD, new_hashedPassword));

                vertx.eventBus().request(Services.DB_UPDATE, updateMessage, res -> {
                    if (res.failed()) {
                        logger.error(res.cause(), res.cause());
                        promise.fail(res.cause());
                    } else {
                        promise.complete((JsonObject) res.result().body());
                    }
                });
                return promise.future();
            }).onComplete( ar -> {
                if (ar.succeeded()) {
                    logger.debug("consumer -service `" + Services.USER_MODIFY_PASSWORD + "`- , result: " + ar.result());
                    hdlrModifyUser.reply(ar.result());
                } else {
                    hdlrModifyUser.fail(((ReplyException) ar.cause()).failureCode(), ar.cause().getMessage());
                }
            });

        } catch (Exception e) {
            logger.error(e, e);
            hdlrModifyUser.fail(Exceptions.TECHNICAL_ERROR.failureCode(),
                    Exceptions.TECHNICAL_ERROR.getMessage());
        }
    }

    /**
     * @param password String
     * @param salt     String
     *                 <p>
     *                 compute hash of password based on salt and algorithm "SHA-512"
     *                 </p>
     */
    private String computeHash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            String concat = (salt == null ? "" : salt) + password;
            byte[] bHash = md.digest(concat.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bHash);
        } catch (NoSuchAlgorithmException e) {
            throw new VertxException(e);
        }
    }

    private Future<String> sendMail(JsonObject msg) {
        Promise<String> promise = Promise.promise();
        try {
            if(msg.containsKey("to")) {
                MailConfig config = new MailConfig(config().getJsonObject("mail"));

                MailClient mailClient = MailClient.create(vertx, config);
                MailMessage message = new MailMessage();
                message.setFrom(config.getUsername());
                message.setTo(msg.getString("to"));
                message.setSubject(msg.getString("subject","GP-DEVOX new account"));
                if(msg.containsKey("password"))
                    message.setHtml("<p>Un nouveau compte a été crée dans "+msg.getString("url", "GP-Devox")+" </p><br><p>Nom d'utilisateur: <b>"+msg.getString("username")+"</b></p><p>Mot de passe: <b>"+ msg.getString("password")+"</b></p><br><p>Veuillez changer votre mot de passe le plus tôt possible<p>");

                if (msg.containsKey("body"))
                    message.setHtml(msg.getString("body"));

                mailClient.sendMail(message, result -> {
                    logger.debug("sending mail");
                    if (result.succeeded()) {
                        logger.debug("mail sent successfuly");
                        promise.complete("succeeded");
                    } else {
                        logger.error("error sending mail");
                        promise.fail(Exceptions.TECHNICAL_ERROR);
                    }
                });
            }else{
                logger.error("no info given");
                promise.fail(Exceptions.TECHNICAL_ERROR);
            }
        } catch (Exception e) {
            logger.error(e, e);
            promise.fail(Exceptions.TECHNICAL_ERROR);
        }
        return promise.future();
    }

    public String generatePassword(int...len) {
        logger.debug("generatePassword");

        int pwd_length = len.length > 0 ? len[0] : 8;
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String pwd = "";

        for (int i = 0; i < pwd_length; i++)
        {
            pwd += chars.charAt((int) Math.floor(Math.random() * chars.length()));
        }

        return pwd;
    }

    /**
     * @param bytes Array
     * @return String of Hex
     * <p>
     * transform an array of bytes to String
     * </p>
     */
    private String bytesToHex(byte[] bytes) {
        char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int x = 0xFF & bytes[i];
            chars[i * 2] = HEX_CHARS[x >>> 4];
            chars[1 + i * 2] = HEX_CHARS[0x0F & x];
        }
        return new String(chars);
    }

}
