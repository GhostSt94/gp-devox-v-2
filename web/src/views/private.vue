<template>
  <div id="private">

    <sidebar />


    <div id="private-content">

      <navbar />

      <div class="main">
        <div :class="LOADING ? 'position-absolute w-100 h-100 opacity-75 bg-light d-flex justify-content-center align-items-center loader' : 'd-none'">
          <div class="spinner-border" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
        </div>


        <transition name="fade" mode="out-in">
          <router-view :key="$route.fullPath"></router-view>
        </transition>
      </div>

    </div>
  </div>
</template>

<script>
import Sidebar from '@/components/Sidebar.vue'
import Navbar from '@/components/Navbar.vue'

	export default {
		name: "Private",
    components:{
      Sidebar, Navbar
    },
		data() {
			return {};
		},
		computed: {
			LOADING() {
				return this.$store.state.loading;
			}
		},
		methods: {
			logout() {
				log("`logout`");
				this.$store.dispatchAsync('/logout', {}).then(() => {
					this.$store.commit('SET_CURRENT_USER', {});
					this.$router.push("/");
				});
			},

		},
		mounted() {
			this.$store.dispatch('initApp')
		}
	}
</script>

<style scoped>
/* CONTENT */
*{
  font-size: 1rem;
}
#private-content {
  position: relative;
  width: calc(100% - 280px);
  left: 280px;
  transition: .3s ease;
}
#sidebar.hide ~ #private-content {
  width: calc(100% - 60px);
  left: 60px;
}
.loader {
  z-index: 100;
}
.spinner-border{
  color: var(--color-2);
}
.main {
  width: 100%;
  padding: 36px 24px;
  font-family: var(--poppins);
  max-height: calc(100vh - 56px);
  overflow-y: auto;
}
#private {
  background: var(--grey) !important;
  min-height: 100vh;
}
</style>
