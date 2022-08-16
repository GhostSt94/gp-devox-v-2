<template>
	<div id="app">
		<notifications group="user-message" position="right top" :speed="1" :duration="2500" class="user-message"/>
		<router-view></router-view>
	</div>
</template>

<script>
	// import semantic-ui dependencies
	/*import '../libs/semantic-ui/semantic.min.js';
	import '../libs/semantic-ui/semantic.min.css';
import "vue-pdf-app/dist/icons/main.css";*/
	export default {
		name: 'App',
	}
</script>

<style>
	@import './assets/css/style.css';
	@import './assets/css/vue-select.css';

  html{
    height:100%;
  }
</style>
