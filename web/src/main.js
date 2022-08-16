import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import shared from './shared'
import moment from 'moment'

Vue.config.productionTip = false;
Vue.prototype.$SHARED = shared;
Vue.prototype.$moment = moment

// notifications
import Notifications from 'vue-notification'
Vue.use(Notifications);

// vue-select
import vSelect from 'vue-select'
Vue.component('v-select', vSelect);

// modal
import VModal from 'vue-js-modal'
Vue.use(VModal, {dialog: true});

// Global event bus
Vue.prototype.$eb = new Vue();

// session middleware
// check if authenticated if not redirect to /login
// const private_regex = /(\/private\/)+/gm;

// router.beforeEach((to, from, next) => {
// 	if(_.isEmpty(store.state.user) && to.name !== 'login'){
// 		store.dispatchAsync("/getSessionUser", {}, true, false,true).then(
// 			data => {
// 				// set store user
// 				store.commit("SET_CURRENT_USER", data);
// 				console.log(store.state.user);
// 				next()
// 			},
// 			err => {
// 				if(private_regex.test(to.path)) {
// 					console.log("redirect to login");
// 					next({name:'login'})
// 				} else {
// 					next();
// 				}
// 			}
// 		);
// 	}
// 	else{
// 		next()
// 	}
// });

new Vue({
	router,
	store,
	render: h => h(App)
}).$mount('#app');
