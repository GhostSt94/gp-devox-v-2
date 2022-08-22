<template>
  <div class="container-fluid">
    <div class="row align-items-center">

      <div class="col-md-6 div-logo d-flex align-items-center justify-content-center">
        <div class="text-center div-img">
          <img src="@/assets/images/logo_devox2.png" alt="image">
          <div class="blur"></div>
        </div>
      </div>

      <div class="col-md-6 d-flex justify-content-center">
        <transition name="fade" mode="out-in">
          <router-view :key="$route.fullPath"></router-view>
        </transition>
      </div>
    </div>
  </div>
</template>

<script>

export default {
}
</script>

<style scoped>
.row{ height: 100vh;width: 100vw; }
.dynamic-div{ width: min(80%, 400px);background: white; }
.div-logo{
  height: 100%;
  background: var(--color-3);
}
.div-img{
  position: relative;
  width: 50%;
  height: 10%;
}
img{
  position: absolute;
  top: 50%;
  left:50%;
  transform: translate(-50%,-50%);
  width: 100%;
}
.blur{
  position: absolute;
  bottom: -75%;
  right:-10%;
  width: 120%;
  height: 120%;
  backdrop-filter: blur(6px);
  filter: blur(5px);
  border-bottom-left-radius: 50%;
  border-bottom-right-radius: 50%;
  border:3px solid var(--color-4)
}

@media screen and (max-width:700px) {

  .div-logo{ height: 20vh }
  .dynamic-div{ height: 80vh }

  .blur{ height: 180%; bottom:-150%}
}
</style>