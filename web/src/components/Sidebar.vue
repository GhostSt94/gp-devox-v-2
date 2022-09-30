<template>
<!-- SIDEBAR -->
	<section id="sidebar">
        <div class="text-center menu-header">
			<a href="/" class="brand py-3">
				<img src="@/assets/images/logo_devox2.png" alt="logo">
			</a>
        </div>
		
		<ul class="side-menu top">
			<li :class="{ 'active': $route.name === 'home' }">
				<router-link :to="{name:'home'}">
					<i class="bi bi-grid"></i>
					<span class="text">Dashboard</span>
				</router-link>
			</li>
			<li :class="{ 'active': projectRoutes.some(e => e === $route.name) }">
				<router-link :to="{name:'projects'}">
					<i class="bi bi-list-stars"></i>
					<span class="text">Projets</span>
				</router-link>
			</li>
			<li :class="{ 'active': $route.name === 'clients' }">
				<router-link :to="{name:'clients'}">
					<i class="bi bi-building"></i>
					<span class="text">Clients</span>
				</router-link>
			</li>
			<li :class="{ 'active': $route.name === 'users' }">
				<router-link :to="{name:'users'}">
					<i class='bi bi-people-fill' ></i>
					<span class="text">Utilisateurs</span>
				</router-link>
			</li>
		</ul>
		<ul class="side-menu">
			<!-- <li>
				<a href="#">
					<i class='bx bxs-cog' ></i>
					<span class="text">Settings</span>
				</a>
			</li> -->
			<li>
				<a @click="logout($event)" href="#" class="logout">
					<i class="bi bi-box-arrow-left"></i>
					<span class="text">Logout</span>
				</a>
			</li>
		</ul>
	</section>
</template>

<script>
export default {
    name:"sidebar",
	data(){
		return{
			projectRoutes: ['projects', 'addProject', 'projectDetails']
		}
	},
    methods: {
        logout(e) {
			      e.preventDefault()
            console.log("`logout`");
            this.$store.dispatchAsync('/logout', {}).then(() => {
              this.$store.commit('SET_CURRENT_USER', {});
              this.$router.push("/");
            });
        },
    },	
    mounted(){
        // TOGGLE SIDEBAR
        const menuBar = document.querySelector('.toggle');
        const sidebar = document.getElementById('sidebar');

        menuBar.addEventListener('click', function () {
            sidebar.classList.toggle('hide');
        })
        
        if(window.innerWidth < 768) {
            sidebar.classList.add('hide');
        }
    }
}
</script>

<style>

#sidebar * {
	margin: 0;
	padding: 0;
	box-sizing: border-box;
}

html {
	overflow-x: hidden;
}

body {
	background: white;
	overflow-x: hidden;
}

/* SIDEBAR */
#sidebar {
	position: fixed;
	top: 0;
	left: 0;
	width: 280px;
	height: 100%;
	background: var(--light);
	z-index: 2000;
	font-family: var(--poppins);
	transition: .3s ease;
	overflow-x: hidden;
	scrollbar-width: none;
}
#sidebar::--webkit-scrollbar {
	display: none;
}
#sidebar.hide {
	width: 60px;
}
/* image */
#sidebar.hide img{
    width: 90%;
}
#sidebar .brand {
	display: flex;
    justify-content: center;
}
#sidebar .brand img {
	width: 70%;
}
/* side menu */
#sidebar.hide ul{
    padding-left: 5px;
}
#sidebar .side-menu {
	width: 100%;
	margin-top: 48px;
}
#sidebar .side-menu li {
	height: 48px;
	background: transparent;
	margin-left: 10px;
	border-radius: 48px 0 0 48px;
	padding: 4px;
}
#sidebar .side-menu li.active {
	background: var(--grey);
	position: relative;
}
#sidebar .side-menu li.active::before {
	content: '';
	position: absolute;
	width: 40px;
	height: 40px;
	border-radius: 50%;
	top: -40px;
	right: 0;
	box-shadow: 20px 20px 0 var(--grey);
	z-index: -1;
}
#sidebar .side-menu li.active::after {
	content: '';
	position: absolute;
	width: 40px;
	height: 40px;
	border-radius: 50%;
	bottom: -40px;
	right: 0;
	box-shadow: 20px -20px 0 var(--grey);
	z-index: -1;
}
#sidebar .side-menu li a {
	width: 100%;
	height: 100%;
	background: var(--light);
	display: flex;
	align-items: center;
	border-radius: 48px;
	font-size: 16px;
	color: var(--dark);
	white-space: nowrap;
	overflow-x: hidden;
  font-size: 1.2rem !important;
}
#sidebar .side-menu.top li.active a {
	color: var(--color-1);
}
#sidebar.hide .side-menu li a {
	width: calc(48px - (4px * 2));
	transition: width .3s ease;
}
#sidebar a.logout {
	color: var(--red) !important;
}
#sidebar a.logout:hover .bi{
  transform: rotateX(360deg);
  transition: .6s ease;
}
#sidebar .side-menu.top li a:hover {
	color: var(--color-1);
}
#sidebar .side-menu li a .bi {
	min-width: calc(60px  - ((4px + 6px) * 2));
	display: flex;
	justify-content: center;
}
/* SIDEBAR */
</style>