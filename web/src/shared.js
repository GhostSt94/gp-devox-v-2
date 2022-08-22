import moment from "moment";

const fields = {
};

const services = {
	login: '/login',
	checkSession: '/getSessionUser',
	isConnected: '/is_connected',
	user: {
		list: '/private/user/list',
		create: '/private/user/create',
		update: '/private/user/update',
		delete: '/private/user/remove',
		changePassword: '/private/user/password/change',
		generetePassword: '/private/user/password/generate',
		resetPassword: '/user/password/reset/',
		newPassword: '/user/password/new',
	},
	attachement:{
		list:'/private/attachement/list',
		create: '/private/attachement/create',
		update: '/private/attachement/update',
		delete: '/private/attachement/remove'
	},
	project:{
		list:'/private/project/list',
		create: '/private/project/create',
		update: '/private/project/update',
		delete: '/private/project/remove',
		stats: '/private/project/stats'
	},
	client:{
		list:'/private/client/list',
		create: '/private/client/create',
		update: '/private/client/update',
		delete: '/private/client/remove'
	},
	facture:{
		list:'/private/facture/list',
		create: '/private/facture/create',
		update: '/private/facture/update',
		delete: '/private/facture/remove',
		stats: '/private/facture/stats'
	},
	comment:{
		list:'/private/comment/list',
		create: '/private/comment/create',
		delete: '/private/comment/remove'
	},
	upload: '/private/attachement/upload'
};

const messages = {
	// server error codes
	'-1': "TECHNICAL_ERROR", // event bus replay delay
	0: "TECHNICAL_ERROR",
	1: "USER_OR_PASSWORD_INCORRECT",
	2: "DUPLICATED_USERNAME",
	3: "WRONG_PASSWORD",
	4: "SESSION_EXPIRED",
	5: "LIEN EXPIRER",
	6: "DOCUMENT HAS ALREADY AN OPEN TASK",
	_200:"fichier  charger avec success",

	technical_error: "TECHNICAL_ERROR",

	user: {
		create_succeeded: "L'utilisateur a été créé avec succès, un mail contenant un mot de passe a été envoyé.",
		update_succeeded: "utilisateur mis à jour avec succès!",
		delete_succeeded: "utilisateur supprimé avec succès!",
	},
	upload:{
		upload_succeeded: "Les pièces jointes sont telechargées avec succès!"
	},
	facture:{
		create_succeeded: "La facture a été crée avec succès!",
		delete_succeeded: 'La facture a été Supprimée avec succès!'
	},
	project:{
		create_succeeded: "Le projet a été Ajouté avec succès!",
		update_succeeded: "Le projet a été Modifié avec succès!",
		delete_succeeded: 'Le projet a été Supprimé avec succès!'
	},
	comment:{
		create_succeeded: "Commentaire ajouté!"
	},
	update_success: "Modifié avec succés",
	resetPassword: 'Un mail contenant le lien pour reinitialiser votre mot de passe a été envoyé'
};
const collections = {
	user: "user",
	etudiants: "etudiants"
};

const utils = {
	// current date or parse to milliseconds
	getStartOfDay: function (date) {
		return moment(date).startOf('day').valueOf();
	},
	getEndOfDay: function (date) {
		return moment(date).endOf('day').valueOf();
	},
	getMilliseconds: function (date = null) {
		return date === null ? new Date().getTime() : moment(date).utc().valueOf();

		// var milliseconds = Date.now()
		// var milliseconds = new Date().getTime()
		// var milliseconds = $.now()
		// var milliseconds = moment().utc().valueOf()

		// var timestamp = moment().utc().unix()
		// var timestamp_string = moment().utc().format("X")
	},
	// milliseconds to date as ISO8601
	getDateAsIso: function (milliseconds) {
		if (milliseconds === '' || milliseconds === undefined)
			return '';
		return moment(milliseconds).utc().toISOString();
	},
	// milliseconds to date
	getDate: function (milliseconds, format = 'DD/MM/YYYY HH:mm') {
		if (typeof milliseconds === "number")
			return moment(milliseconds).format(format);
	},
	getDateOnly: function (milliseconds, format = 'DD/MM/YYYY') {
		if (typeof milliseconds === "number")
			return moment(milliseconds).format(format);
	},
	// get Dates Difference (milliseconds)
	getDatesDifference: function (start, end) {
		if (!start || !end)
			return ''
		const startDate = moment.utc(start);
		const endDate = moment.utc(end);
		const diff = endDate.diff(startDate);
		const duration = moment.duration(diff);
		const days = duration.get('days');
		const hours = duration.get('hours');
		const minutes = duration.get('minutes');
		let time = '';
		if (days > 0)
			time += days > 10 ? `${days}j ,` : `0${days}j ,`;
		time += hours > 10 ? `${hours}h` : `0${hours}h`
		time += minutes > 10 ? `${minutes}m` : `0${minutes}m`
		return time;
	},
	// passed time from a date in milliseconds
	getTimeFromNow: milliseconds => moment(milliseconds).fromNow(),

	numberToMoneyFormat: price => Intl.NumberFormat('de-DE').format(price)+' DH',
	generatePassword: ()=> {
		var length = 8,
			charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
			retVal = "";
		for (var i = 0, n = charset.length; i < length; ++i) {
			retVal += charset.charAt(Math.floor(Math.random() * n));
		}
		return retVal;
	},
	checkIfMail: txt =>{
		if(!/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/.test(txt)){
			return false
		}else{
			return true
		}
	}
};

const consts={
	loading:"LOADING",
	noData:"NO DATA FOUND"
}

export default {
	fields,
	services,
	messages,
	collections,
	utils,
	consts
};