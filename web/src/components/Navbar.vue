<template>
    <nav  class="d-flex justify-content-between">
        <i class="bi bi-list toggle"></i>
        <!-- <a href="#" class="nav-link search">Categories</a>
        <form action="#" class="search">
            <div class="form-input">
                <input type="search" placeholder="Search...">
                <button type="submit" class="search-btn"><i class='bx bx-search' ></i></button>
            </div>
        </form> -->
        <!-- <input type="checkbox" id="switch-mode" hidden>
        <label for="switch-mode" class="switch-mode"></label> -->
        <div :class="{ 'active': $route.name === 'profile' }" class="profile-container pointer" @click="$route.name != 'profile' && $router.push({name: 'profile'})">
            <h5 class="d-inline mx-2">
                {{ CURRENT_USER.username }}
            </h5>
            <a href="#" class="profile">
                <img src="@/assets/images/profile.png">
            </a>
        </div>
    </nav>
</template>

<script>
export default {
    name: 'navbar',
  computed:{
      CURRENT_USER(){return this.$store.state.current_user}
  }
}
</script>

<style scoped>
    /* NAVBAR */
nav {
	height: 56px;
	background: var(--light);
	padding: 0 ;
	display: flex;
	align-items: center;
	grid-gap: 24px;
	font-family: var(--lato);
	position: sticky;
	top: 0;
	left: 0;
}
nav::before {
	content: '';
	position: absolute;
	width: 40px;
	height: 40px;
	bottom: -40px;
	left: 0;
	border-radius: 50%;
	box-shadow: -20px -20px 0 var(--light);
}

nav .bi.toggle{
    font-size: 2.5rem;
    cursor: pointer;
}
nav a {
	color: var(--dark);
}
 nav .bx.bx-menu {
	cursor: pointer;
	color: var(--dark);
}
 nav .nav-link {
	font-size: 16px;
	transition: .3s ease;
}
 nav .nav-link:hover {
	color: var(--blue);
}
    /*nav form {
     max-width: 400px;
     width: 100%;
     margin-right: auto;
   }
    nav form .form-input {
     display: flex;
     align-items: cenr;
     height: 36px;
   }
       /* nav form .form-input input {
         flex-grow: 1;
         padding: 0 16px;
         height: 100%;
         border: none;
         background: var(--grey);
         border-radius: 36px 0 0 36px;
         outline: none;
         width: 100%;
         color: var(--dark);
       }
        nav form .form-input button {
         width: 36px;
         height: 100%;
         display: flex;
         justify-content: center;
         align-items: center;
         background: var(--blue);
         color: var(--light);
         font-size:8px;
         border: none;
         outline: none;
         border-radius: 0 36px 36px 0;
         cursor: pointer;
       }
           /*nav .notification {
            font-size: 20px;
            position: relave;
          }
          /* nav .notification .num {
            position: absolute;
            top: -6px;
            right: -6px;
            width: 20px;
            height: 20px;
            border-radius: 50%;
            border: 2px solid var(--light);
            background: var(--red);
            color: var(--light);
            font-weight: 700;
            font-size: 12px;
            display: flex;
            justify-content: center;
            align-items: center;
          }*/
 nav .profile img {
	width: 36px;
	height: 36px;
	object-fit: cover;
	border-radius: 50%;
}
 .profile-container{
   padding: 10px;
 }
 .profile-container:hover, .profile-container.active{
   border-bottom-left-radius: 30px;
   border-top-left-radius: 30px;
   box-shadow: -8px 0px 0px 0px var(--grey);
   color: var(--color-1);
   transition: .3s ease;
 }
/* NAVBAR */
</style>