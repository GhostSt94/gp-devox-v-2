package com.utils.constants;

public class Services {

	/*----------------------------------
	*               upload
	----------------------------------*/
	public static final String UPLOAD = "com.services.upload";
    public static final String REMOVE_UPLOAD = "com.services.upload.remove";

    /*----------------------------------
    *               DB
    ----------------------------------*/
    public static final String DB_FIND = "find";
    public static final String DB_FIND_ONE = "findOne";
    public static final String DB_INSERT = "insert";
    public static final String DB_UPDATE = "update";
    public static final String DB_UPDATE_COLLECTION_WITH_OPTIONS = "updateCollectionWithOptions";
    public static final String DB_UPDATE_ONE_AND_RETURN = "updateAndReturn";
    public static final String DB_REMOVE_DOCUMENT = "removeDocument";
    public static final String DB_COUNT = "count";
    public static final String DB_BULK_WRITE = "bulk";
    public static final String DB_RUN_COMMAND = "runCommand";
    public static final String DB_DISTINCT = "distinct";
    public static final String DB_FIND_WITH_COUNT = "findWithCount";

	/*----------------------------------
	*               user
	----------------------------------*/
	public static final String USER_LIST = "getListUsers";
	public static final String USER_REMOVE = "removeUser";
	public static final String USER_CREATE= "createUser";
	public static final String USER_MODIFY = "modifyUser";
	public static final String USER_MODIFY_PASSWORD = "userModifyPassword";
    public static final String GENERATE_NEW_PASSWORD = "generateNewPassword";
    public static final String RESET_PASSWORD = "resetPassword";
    public static final String NEW_PASSWORD = "newPassword";

    /*----------------------------------
    *               PROJECT
    ----------------------------------*/
    public static final String PROJECT_LIST = "getlistProject";
    public static final String PROJECT_REMOVE = "removeProject";
    public static final String PROJECT_CREATE= "createProject";
    public static final String PROJECT_MODIFY = "modifyProject";
    public static final String PROJECT_STATS = "statsProject";

    /*----------------------------------
	*               Facture
	----------------------------------*/
    public static final String FACTURE_LIST = "getListFacture";
    public static final String FACTURE_REMOVE = "removeFacture";
    public static final String FACTURE_CREATE = "createFacture";
    public static final String FACTURE_MODIFY = "modifyFacture";
    public static final String FACTURE_STATS = "statsFacture";

    /*----------------------------------
	*               Client
	----------------------------------*/
    public static final String CLIENT_LIST = "getListClient";
    public static final String CLIENT_REMOVE = "removeClient";
    public static final String CLIENT_CREATE= "createClient";
    public static final String CLIENT_MODIFY = "modifyClient";

    /*----------------------------------
	*               Comment
	----------------------------------*/
    public static final String COMMENT_LIST = "getListComment";
    public static final String COMMENT_REMOVE = "removeComment";
    public static final String COMMENT_CREATE= "createComment";

    /*----------------------------------
    *               Attachement
    ----------------------------------*/
    public static final String ATTACHEMENT_LIST = "listAttachement";
    public static final String ATTACHEMENT_REMOVE = "removeAttachement";
    public static final String ATTACHEMENT_CREATE = "createAttachement";


}
