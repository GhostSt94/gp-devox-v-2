<template>
  <div class="container-fluid w-100 row justify-content-center align-items-center">
    <div class="col-md-4 card p-0">
        <div class="card-header text-center">
          <h4>Changer mot de passe</h4>
        </div>
        <div class="card-body p-4">
          <div class="mb-4">
            <label class="form-lable required m-1">Entrez votre ancien mot de passe</label>
            <input v-model="oldPassword" type="password" placeholder="Ancien Mot de passe" class="form-control">
          </div>
          <div class="mb-4">
            <label class="form-lable required m-1">Entrez votre nouveau mot de passe</label>
            <input v-model="newPassword" type="password" placeholder="Nouveau Mot de passe" class="form-control">
          </div>
          <div class="mb-4">
            <label class="form-lable required m-1">Confirmer mot de passe</label>
            <input v-model="confirmPassword" type="password" placeholder="confirmer mot de passe" class="form-control">
          </div>
          <button @click="changePassword" class="btn btn-primary w-100">Submit</button>
        </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "changePassword",
  data(){
    return{
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  },
  computed:{
    CURRENT_USER(){ return this.$store.state.current_user}
  },
  methods:{
    changePassword(){
      console.log('changePassword')

      if(this.oldPassword === '' || this.newPassword === '' || this.confirmPassword === ''){
        this.$store.errorNotif('Veuillez remplir tous les champs')
        return
      }

      if(this.newPassword !== this.confirmPassword){
        this.$store.errorNotif('les mots de passe se ressemble pas')
        return
      }

      if(this.newPassword.length < 6){
        this.$store.errorNotif('Mot de passe trop court! minimun 6 caractères')
        return
      }

      let data= {
        user_id: this.CURRENT_USER._id,
        oldPassword: this.oldPassword,
        newPassword: this.newPassword
      }

      this.$store.dispatchAsync(this.$SHARED.services.user.changePassword, data).then(ar=>{
        console.log('changePassword', ar);

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