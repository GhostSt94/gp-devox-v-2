package com.utils.constants;

public class Fields {


	/*----------------------------------
	*               global
	----------------------------------*/
	// timestamp of document insertion in DB
	public static final String TMSP = "tmsp";
	// response flux
	public static final String RESPONSE_FLUX_SUCCEEDED = "succeeded";
	public static final String RESPONSE_FLUX_DATA = "data";
	public static final String RESPONSE_FLUX_MESSAGE = "message";

	/*----------------------------------
	*               user
	----------------------------------*/
	public static final String USER_USERNAME = "username";
	public static final String USER_PASSWORD = "password";
	public static final String USER_SALT = "salt";
	public static final String USER_ROLE = "roles";
	public static final String USER_PERMISSIONS = "permissions";
	public static final String USER_DATE_CREATION = "date_creation";

	/*----------------------------------
	*               Project
	----------------------------------*/
	public static final String PROJECT_SOCIETE = "societe";
	public static final String PROJECT_NAME = "name";
	public static final String PROJECT_DATE_DEBUT = "dateDebut";
	public static final String PROJECT_DATE_FIN = "dateFin";
	public static final String PROJECT_STATUS = "status";
	public static final String PROJECT_TYPE_COMMANDE = "typeCommande";
	public static final String PROJECT_MONTANT = "montant";
	public static final String PROJECT_GARANTIE = "garantie";
	public static final String PROJECT_CREATED_AT = "createdAt";
	public static final String PROJECT_USER = "user";


	/*----------------------------------
	*               CLient
	----------------------------------*/
	public static final String CLIENT_NAME = "name";
	public static final String CLIENT_VILLE = "ville";
	public static final String CLIENT_EMAIL = "email";
	public static final String CLIENT_TEL = "tel";
	public static final String CLIENT_CREATED_AT = "createdAt";
	public static final String CLIENT_USER = "user";

	/*----------------------------------
	*               Attachment
	----------------------------------*/
	public static final String ATTACHMENT_TYPE = "type";
	public static final String ATTACHMENT_NAME = "name";
	public static final String ATTACHMENT_LINK = "link";
	public static final String ATTACHMENT_PATH = "path";
	public static final String ATTACHMENT_ID_PROJECT = "id_dossier";
	public static final String ATTACHMENT_ID_FACTURE = "id_facture";

}
