<template>
  <div class="container">
    <div class="row mb-2">
        <div class="col">
            <h5>Profile</h5>
        </div>
        <div class="col text-end">
            <div class="dropdown">
                  <button class="btn setting" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                    <i class="bi bi-gear-fill"></i>
                  </button>
                  <ul class="dropdown-menu">
                    <li  @click="edit= true"><span class="dropdown-item">Modifier info</span></li>
                    <div class="dropdown-divider"></div>
                    <li @click="$router.push({name: 'changePassword'})"><span class="dropdown-item text-danger">Changer Password</span></li>
                  </ul>
                </div>
        </div>
    </div>
    <div class="info row mb-3">
        <div class="col-auto">
            <img src="@/assets/images/profile.png" alt="profile" class="rounded" height="100">
        </div>
        <div class="col p-3">
            <h3>{{ user.username }}</h3>
            <label>{{ user.role }}</label>
        </div>
    </div>
    <div v-if="!edit" class="row detail">
        <div class="col-auto p-5">
            <label>Nom</label>
            <h5>{{ user.nom ? user.nom : '----'  }}</h5>
        </div>
        <div class="col-auto p-5">
            <label>Prénom</label>
            <h5>{{ user.prenom ? user.prenom : '----'  }}</h5>
        </div>
        <div class="col-auto p-5">
            <label>Mail</label>
            <h5>{{ user.email }}</h5>
        </div>
    </div>
    <div v-else class="row detail">
        <div class="col-auto p-5">
            <label>Nom</label>
            <input type="text" v-model="nom" class="form-control">
        </div>
        <div class="col-auto p-5">
            <label>Prénom</label>
            <input type="text" v-model="prenom" class="form-control">
        </div>
<!--        <div class="col-auto p-5">-->
<!--            <label>Username</label>-->
<!--            <input type="text" v-model="username" class="form-control">-->
<!--        </div>-->
        <div class="col-12 text-end p-5">
          <button @click="editUser" class="btn btn-secondary me-2">submit</button>
          <button @click="edit= false" class="btn btn-info">annuler</button>
        </div>
    </div>
  </div>
</template>

<script>
export default {
    name: 'profile',
    data(){
        return{
          user: {},
            edit: false,
            nom: '',
            prenom: '',
            username: ''
        }
    },
    computed:{
      CURRENT_USER(){return this.$store.state.current_user}
    },
    methods:{
        getUser(){
          let data= {
            query: { _id: this.CURRENT_USER._id},
          }

          this.$store.dispatchAsync(this.$SHARED.services.user.list, data).then(ar=>{
            console.log('user', ar)
            this.user = ar.docs[0]
          })
        },
        editUser(){
            console.log('editUser');

            if(this.nom === '' && this.prenom === ''){
                this.$store.errorNotif("veuillez entrer un champ")
                return
            }

            let data = {
                _id: this.CURRENT_USER._id
            }

            if(this.nom !== '') data.nom = this.nom
            if(this.username !== '') data.username = this.username
            if(this.prenom !== '') data.prenom = this.prenom

            this.$store.dispatchAsync(this.$SHARED.services.user.update, data).then(ar=>{
                this.edit= false
                this.$store.successNotif('Profile updated')
                this.getUser()
            })
        }
    },
  mounted() {
      this.getUser()
  }
}
</script>

<style scoped>
.info{
    border-left: 8px solid var(--blue);
}
.info :is(h3, label, p){
    font-family: var(--poppins) !important;
}
.detail label {
    color: var(--blue);
}
.bi-gear-fill{
    font-size: 1.5rem;
    transition: .4s ease;
}
.bi-gear-fill:hover{
  color: var(--blue);
  font-size: 1.52rem;
}
</style>