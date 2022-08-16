<template>
  <div class="container-fluid w-100 row justify-content-center align-items-center">
    <div class="col-md-4 card p-0">
      <div class="card-header text-center">
        <h4>Changer mot de passe</h4>
      </div>
      <div class="card-body p-4">
        <div class="mb-4">
          <label class="form-lable required m-1">Entrez votre nouveau mot de passe</label>
          <input v-model="newPassword" type="password" class="form-control">
        </div>
        <div class="mb-4">
          <label class="form-lable required m-1">Confirmer mot de passe</label>
          <input v-model="confirmPassword" type="password" class="form-control">
        </div>
        <button @click="resetPassword" class="btn btn-primary w-100">Submit</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "resetPassword",
  data(){
    return{
      newPassword: '',
      confirmPassword: ''
    }
  },
  methods:{
    resetPassword(){
      console.log('resetPassword')
      if (this.newPassword === '' || this.confirmPassword === '') {
        this.$store.errorNotif('Veuillez remplir tous les champs')
        return
      }

      if (this.newPassword !== this.confirmPassword) {
        this.$store.errorNotif('les mots de passe se ressemble pas')
        return
      }

      if (this.newPassword.length < 6) {
        this.$store.errorNotif('Mot de passe trop court! minimun 6 caractères')
        return
      }

      let data = {
        password: this.newPassword,
        userId: this.$route.params.userId,
        resetPin: this.$route.params.resetPin
      }

      this.$store.dispatchAsync(this.$SHARED.services.user.newPassword, data).then(ar => {
        console.log('resetPassword', ar)

        this.$store.successNotif("Mot de passe modifié")
        this.$router.push({name: 'login'})
      })
    }
  }
}
</script>

<style scoped>
.container-fluid{
  margin:0;
  height: 100vh;
  background: var(--grey);
}
.card-header{
  background: var(--color-3);
}
</style>