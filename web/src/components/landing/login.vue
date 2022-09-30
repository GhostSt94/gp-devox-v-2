<template>
    <div class="dynamic-div">
        <div class="py-3">
            <h1>Bienvenu</h1>
            <p>Veuillez saisir vos coordonnées.</p>
            </div>
            <div>
            <div class="mb-3">
                <label for="email" class="form-label">Nom d'utilisateur</label>
                <input type="text" v-model="username" class="form-control" id="email" placeholder="Enter Email here">
            </div>
            <div>
                <label for="pwd" class="form-label">Mot de passe</label>
                <input type="password" v-model="pwd" class="form-control" id="pwd" placeholder="Enter password here">
            </div>
            <div class="text-end">
                <small @click="$modal.show('forget-password-modal')" class="pointer h-underline">Mot de passe oublié ?</small>
            </div>
            <div class="d-grid my-3">
                <button @click="login" class="btn btn-primary">Se Connecter</button>
            </div>
        </div>

      <!-- modal -->
      <modal name="forget-password-modal" width="400px" style="border-radius: 20px;z-index:2000;" :adaptive="true"  height="auto" >
        <div class="container p-3">
          <h3>Mot de passe oublié</h3>
          <hr>
          <div class="row g-3">
            <div class="col-12">
              <label for="username" class="form-lable required p-2">Entrer votre Username</label>
              <input v-model="modal_username" type="text" id="username" placeholder="Username" class="form-control">
            </div>
            <div class="d-grid my-3">
              <button @click="forgetPassword" class="btn btn-primary">Submit</button>
            </div>
          </div>
        </div>
      </modal>
    </div>
</template>

<script>
export default {
name:'login',
  data(){
    return{
      username:'',
      pwd:'',
      modal_username: ''
    }
  },
  computed:{
    SERVER_ENV(){ return this.$store.state.server_addr_env_dev }
  },
  methods:{
    login(){
      if(this.username === '' || this.pwd === ''){
        this.$store.errorNotif('Veuillez remplir les champs')
        return
      }

      this.$store.dispatchAsync(this.$SHARED.services.login, {
        username: this.username,
        password: this.pwd
      }).then((data) => {
        this.$store.commit('SET_CURRENT_USER', data)
        this.$router.push({name: 'home'});
      });
    },
    forgetPassword() {
      console.log('forgetPassword')

      let data = {
        url: window.location.origin + this.$SHARED.services.user.resetPassword,
        username: this.modal_username
      }
      this.$store.dispatchAsync(this.$SHARED.services.user.resetPassword, data).then(ar => {
        console.log(ar)

        this.$modal.hide('forget-password-modal')
        this.$store.successNotif(this.$SHARED.messages.resetPassword);
      })
    }
  }
}
</script>

<style scoped>
p { color: grey }
small {
  color: var(--color-2);
  font-size: .8em;
}
</style>