import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '@/store/index.js'

const dashboard = () => import( '@/components/Dashboard');
const projects = () => import( '@/components/project/index');
const addProject = () => import( '@/components/project/add');
const projectDetails = () => import( '@/components/project/details');
const landing = () => import( '@/components/landing/index');
const login = () => import( '@/components/landing/login');

const landingContainer = () => import('@/views/landing');
const viewPrivate = () => import('@/views/private');

Vue.use(VueRouter);

const routes = [
	{
		path: '/',
		component: landingContainer,
		children: [
			{
				path: '',
				component: landing
			},
			{
				path: '/login',
				name: 'login',
				component: login
			},
		]
	},
	{
		path: '/private',
		name: 'private',
		component: viewPrivate,
		beforeEnter: (to, from, next) => {
			console.log("navGard, private")
			store.dispatch("initApp").then(() => {
				next();
			})
		},
		children: [
			{
				path: 'home',
				name: 'home',
				component: dashboard
			},
			{
				path: 'projects',
				name: 'projects',
				component: projects
			},
			{
				path: 'add-project',
				name: 'addProject',
				component: addProject
			},
			{
				path: 'project/:id',
				name: 'projectDetails',
				component: projectDetails,
			},
			{
				path: 'clients',
				name: 'clients',
				component: () => import( '@/components/Clients'),
			},
			{
				path: 'profile',
				name: 'profile',
				component: () => import( '@/components/Profile'),
			},
			{
				path: 'users',
				name: 'users',
				component: () => import( '@/components/Users'),
			},
			{
				path:'*',
				redirect: to => { return '/private/dashboard' }
			}
		]
	},
	{
		path: '/password/change',
		name: 'changePassword',
		component: ()=> import( '@/components/login/changePassword' )
	},
	{
		path: '/user/password/reset/:userId/:resetPin',
		name: 'resetPassword',
		component: ()=> import( '@/components/login/resetPassword' )
	},
	{
		path: '*',
		redirect: to => { return '/' }
	}
];

const router = new VueRouter({
	mode: 'history',
	base: process.env.BASE_URL,
	routes
});

export default router
