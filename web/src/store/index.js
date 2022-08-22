import Vue from 'vue'
import Vuex from 'vuex'
import router from '../router/index.js';

Vue.use(Vuex);
let _vue = new Vue();
const isProduction = process.env.NODE_ENV === 'production';

if (!isProduction) {
	log('DEV');
	// allow ajax cross
	$.ajaxSetup({
		crossDomain: true,
		xhrFields: {
			withCredentials: true
		}
	});
}

const config = require('@/../../config.json');

const server_addr_env_dev = window.location.protocol + '//' + window.location.hostname + ":" + config.serverPort;
const SERVER_ADDRESS = isProduction ? window.location.origin : server_addr_env_dev;

const state = {
	SERVER_ADDRESS,
	server_addr_env_dev,
	loading: false,
	current_user: {},
	societies:["ALFAY","Devox","View Times"],
	allStatus:["Prospecter","En cours","Reception provisoir","Reception définitif","Cloturer"],
	factureStatus:['Prévue','envoyer','payer'],
	typeCommande:['Marché','Bon de commande','Marché cadre'],
	roles:['Administrateur','Moderateur','Employe'],
	clients: []
};

const getters = {
	getSocieties:(state)=>state.societies,
	getAllStatus:(state)=>state.allStatus,
	getTypeCommand:(state)=>state.typeCommande,
	getClients:(state)=>state.clients,
	IS_ADMIN:(state)=>state.current_user.role === "Administrateur"
};

const mutations = {
	SET_LOADING(state, payload) {
		state.loading = payload
	},
	SET_CURRENT_USER(state, payload) {
		state.current_user = payload
	},
	SET_ACTIONS(state, payload) {
		state.actions = payload
	},
	SET_CLIENTS(state, payload) {
		state.clients = payload
	}
};

const actions = {
	initApp({commit, dispatch}) {
		console.log('`$store.initApp`')
		commit("SET_LOADING", true);
		return new Promise((resolve, reject) => {
			dispatch("check_session").then(() => {
				resolve();
			}, () => {
				reject()
			}).finally(() => {
				commit("SET_LOADING", false)
			})
		})
	},
	check_session(context) {
		console.log('`$store.check_session`')
		return new Promise((resolve, reject) => {
			if ('_id' in context.state.current_user) {
				resolve();
			} else {
				dispatchAsync(_vue.$SHARED.services.checkSession, {}).then(data => {
					context.commit("SET_CURRENT_USER", data);
					resolve();
				}, () => {
					router.push({name: "login"});
					reject()
				});
			}
		})
	},
    isConnected(_) {
        console.log('`$store.isConnected`')
        return new Promise((resolve, reject) =>{
            dispatchAsync(_vue.$SHARED.services.isConnected, {}).then(ar=>{
                !ar ? resolve() : reject()
            })
        })
    },
};

let store = new Vuex.Store({
	state,
	getters,
	mutations,
	actions
});

export default store;

/**
 * @param url <String> request url
 * @param data <JSON> request data
 * @param use_loader <Boolean> show the application loader while request is in progress or not
 * @param notifyOnFailure <Boolean> if true and request replay.succeeded=false show notification then reject the code else just reject the code
 * @param debug <Boolean> console request data
 * @description send http request
 *
 */
function dispatchAsync(url, data= {query: {}, options: {}}, use_loader = false, notifyOnFailure = true, debug = true) {
	const stamp = new Date().getTime() + "-" + Math.round(Math.random() * 1000);
	console.log(`[${stamp}] $store.dispatchAsync...`);
	data = JSON.stringify(data);
	url = SERVER_ADDRESS + url;
	return new Promise((resolve, reject) => {
		use_loader && store.commit('SET_LOADING', true);
		let headers = {"Content-Encoding": "gzip", "Content-Type": "application/json; charset=utf-8"};
		$.ajax({
			url,
			headers,
			data,
			type: "POST",
			datatype: "json",
			complete: () => {
				// hide loader
				use_loader && store.commit('SET_LOADING', false);
			}
		}).done(resp => {
			console.log(`[${stamp}] $store.dispatchAsync\n\turl: %s, \n\tdata: %o\n\tresp: %o`, url, debug ? data : '???', resp);
			if (resp.succeeded) {
				resolve(resp.data);
			} else {
				// if session is expiered
				if (resp.message === 4) {
					notify('error', 4)
					router.push({name: "login"});
				}
				if (notifyOnFailure) {
					notify('error', resp.message)
				}
				reject(resp.message);
			}
		}).fail(err => {
			console.error(`[${stamp}] AjaxFail\n$store.dispatchAsync\n\turl: %s, \n\tdata: %o\n\terr: %o`, url, debug ? data : '???', err);
			reject(0);
		});
	})
}

/**
 * @param url <String> request url
 * @param data <JSON> request data
 * @param use_loader <Boolean> show the application loader while request is in progress or not
 * @param notifyOnFailure <Boolean> if true and request replay.succeeded=false show notification then reject the code else just reject the code
 * @param debug <Boolean> console request data
 * @description upload file
 */
function uploadFile(url, formData, use_loader = false, notifyOnFailure = true, debug = true) {
	const stamp = new Date().getTime() + "-" + Math.round(Math.random() * 1000);
	console.log(`[${stamp}] $store.uploadFile...`);
	return new Promise((resolve, reject) => {
		use_loader && store.commit('SET_LOADING', true);
		url = SERVER_ADDRESS + url;
		$.ajax({
			url,
			type: "POST",
			data: formData,
			xhrFields: { withCredentials: true },
			cache: false,
			contentType: false,
			enctype: "multipart/form-data",
			processData: false,
			complete: () => {
				// hide loader
				use_loader && store.commit('SET_LOADING', false);
			}
		}).done(resp => {
			log("uploadFile resp", resp)
			console.log(`[${stamp}] $store.uploadFile\n\turl: %s, \n\tdata: %o\n\tresp: %o`, url, debug ? formData : '???', resp);
			if (resp.succeeded) {
				resolve(resp.data);
			} else {
				// if session is expiered
				if (resp.message === 4) {
					notify('error', 4)
					router.push({name: "login"});
				}
				if (notifyOnFailure) {
					notify('error', resp.message)
				}
				reject(resp.message);
			}
		}).fail(err => {
			console.error(`[${stamp}] AjaxFail\n$store.uploadFile\n\turl: %s, \n\tdata: %o\n\terr: %o`, url, debug ? formData : '???', err);
			reject(0);
		});
	})
}

store.openFile = (url) => {
	console.log('openDocument, url:', url)
	url = SERVER_ADDRESS + '/private' + url
	let a = document.createElement('a')
	a.href = url
	a.target = '_blank'
	document.body.appendChild(a)
	a.click()
	document.body.removeChild(a)
	/*$.ajax({
		type: 'GET',
		url: SERVER_ADDRESS + '/private' + url,
		xhrFields: {
			responseType: 'blob'
		},
		success: function(blob){
			console.log(blob.size);
			var link=document.createElement('a');
			link.href=window.URL.createObjectURL(blob);
			link.download="Dossier_" + new Date() + ".pdf";
			link.click();
		}
	});*/
}
store.successNotif = message => {
	_vue.$notify({
		group: 'user-message',
		type: 'success',
		text: message
	})
}
store.errorNotif = message => {
	_vue.$notify({
		group: 'user-message',
		type: 'error',
		text: message
	})
}

store.dispatchAsync = dispatchAsync;
store.uploadFile = uploadFile;


// TODO remove this
const LAST_NOTIFICATION = {
	code: '',
	date: 0
};

// warn , error, success, info
function notify(type, code) {
	const currentTime = new Date().getTime();
	const sameCode = LAST_NOTIFICATION.code === code
	const sameTime = currentTime - LAST_NOTIFICATION.date < 3000;
	//_vue.$notify({group: 'user-message', clean: true})
	if (sameCode && sameTime) {
	} else {
		_vue.$notify({
			group: 'user-message',
			type: type, // warn , error, success, info
			text: _vue.$SHARED.messages[code]
		});
	}
	LAST_NOTIFICATION.code = code;
	LAST_NOTIFICATION.date = currentTime;
}
