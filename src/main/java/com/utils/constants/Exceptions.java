package com.utils.constants;

import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;

public class Exceptions {

	public static final ReplyException TECHNICAL_ERROR = new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 0, "TECHNICAL_ERROR");

	// login
	public static final ReplyException USER_OR_PASSWORD_INCORRECT = new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 1, "USER_OR_PASSWORD_INCORRECT");
	public static final ReplyException SESSION_EXPIRED = new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 4, "SESSION_EXPIRED");
	// user
	public static final ReplyException DUPLICATED_USERNAME = new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 2, "DUPLICATED_USERNAME");
	public static final ReplyException WRONG_PASSWORD = new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 3, "WRONG_PASSWORD");
	public static final ReplyException RESET_PASSWORD_LINK_EXPIRED = new ReplyException(ReplyFailure.RECIPIENT_FAILURE, 5, "RESET_PASSWORD_LINK_EXPIRED");

}
