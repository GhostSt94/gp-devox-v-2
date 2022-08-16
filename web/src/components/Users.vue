<template>

  <div class="container">
    <div class="row header-content">
      <div class="col">
        <h2>Tous les Utilisateurs</h2>
      </div>
      <div class="col text-end">
        <!-- <span @click="filter = !filter" class="pe-5 h-underline pointer">Filter</span> -->
        <button @click="$modal.show('user-modal')" class="btn btn-primary">
            <i class="plus icon"></i>
            New User
        </button>
      </div>
    </div>

    
    <!-- <div class="container">
      <div class="row my-2 p-1 rounded shadow-sm filter g-2" :class="{'d-none' : !filter}">
        <div class="col">
            <label for="soc" class="form-lable required">Societe</label>
            <select v-model="societe" class="form-select form-select-sm" id="soc">
              <option></option>
              <option v-for="soc in SOCIETE_LIST" :key="soc">{{ soc }}</option>
            </select>
        </div>
        <div class="col">
            <label for="cl" class="form-lable required">Client</label>
            <select v-model="client" class="form-select form-select-sm" id="cl">
              <option></option>
              <option v-for="cl in CLIENTS" :key="cl._id" :value="cl">{{ cl.nom }}</option>
            </select>
        </div>
        <div class="col">
            <label for="st" class="form-lable required">Societe</label>
            <select v-model="status" class="form-select form-select-sm" id="st">
              <option></option>
              <option v-for="st in ALL_STATUS" :key="st">{{ st }}</option>
            </select>
        </div>
        <div class="col-12 text-end">
          <button  @click="getUsers" class="btn btn-secondary">Search</button>
          <span  @click="resetFilter" class="ps-2 h-underline pointer">reset</span>
        </div>
      </div>
    </div> -->
    
    <div class="container py-4">
      <table class="table bg-light">
        <thead>
          <tr>
            <th>Username</th>
            <th>Email</th>
            <th>Rôle</th>
            <th v-show="IS_ADMIN"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u._id">
            <td>{{ u.username }}</td>
            <td>{{ u.email }}</td>
            <td>
              <select v-if="edit === true && editId === u._id" v-model="newRole" class="form-select" style="width: auto">
                <option v-for="r in ROLES" :key="r">{{r}}</option>
              </select>
              <span v-else>{{ u.role }}</span>
            </td>
            <td v-show="IS_ADMIN">
              <div v-if="edit === true && editId === u._id" class="edit">
                <i @click="updateRole(u._id)" class="bi bi-check me-1"></i>
                <i @click="edit= false; editId= null" class="bi bi-x"></i>
              </div>
              <div v-else class="dropdown">
                  <button class="btn" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                    <i class="bi bi-three-dots text-end"></i>
                  </button>
                  <ul class="dropdown-menu">
                    <li @click="edit= true; editId= u._id"><span class="dropdown-item">Modifier Rôle</span></li>
                    <div class="dropdown-divider"></div>
                    <li @click="generatePassword(u)"><span class="dropdown-item text-muted">Reinitialiser mot de passe</span></li>
                    <li @click="deleteUser(u._id)"><span class="dropdown-item text-danger">Supprimer</span></li>
                  </ul>
                </div>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div class="w-100 pt-2 d-flex justify-content-center">
        <pagination-2  v-bind="pagination" @setPage="setPage" />
      </div>
      
    </div>

    <!-- User modal -->
        <modal name="user-modal" width="600px" style="border-radius: 20px;z-index:2000;" :adaptive="true"  height="auto" >
            <div class="container p-3">
              <h2 class="secondary">Ajouter un utilisateur</h2>
              <hr>
              <div class="row g-3">
                <div class="col-12">
                  <label for="username" class="form-lable required">Username</label>
                  <input v-model="user.username" type="text" id="username" placeholder="Username" class="form-control">
                </div>
                <div class="col-12">
                  <label for="mail" class="form-lable required">Email</label>
                  <input v-model="user.email" type="mail" id="mail" placeholder="Email" class="form-control">
                </div>
                <div class="col-12">
                  <label class="form-lable required">Rôle</label>
                  <select v-model="user.role" class="form-select">
                    <option v-for="r in ROLES" :key="r">{{r}}</option>
                  </select>
                </div>
                <div class="col-12 text-end">
                  <div v-if="isBusy" class="spinner-border secondary mx-3" role="status"></div>
                  <button @click="addUser" class="btn btn-secondary">Ajouter</button>
                </div>
              </div>
            </div>
        </modal>
  </div>

</template>

<script>
import Pagination2 from './generic/pagination2.vue'

export default {
  name: 'users',
  components: {
    Pagination2
  },
  data(){
    return{
      users:[],
      pagination: {
        page: 1,
        limit: 10,
        count: 0
      },
      user:{
        username:'',
        email:'',
        role:null
      },
      isBusy: false,
      newRole: null,
      edit: false,
      editId: null
    }
  },
  computed:{
    ROLES(){ return this.$store.state.roles},
    IS_ADMIN(){ return this.$store.state.current_user.role === 'Administrateur'}
  },
  methods: {
    setPage(page){
      console.log("page: "+page)

      this.pagination.page=page
      this.getUsers()
    },
    // resetFilter(){
    //   this.societe= ''
    //   this.client= null
    //   this.status= ''

    //   this.getUsers()
    // },
    getUsers(){
      console.log('getUsers');
      
      let data={
        query:{},
        options:{
          page: this.pagination.page,
          limit: this.pagination.limit,
          withCount: true
        }
      }

      /*if(this.societe!=='')
        data.query={...data.query,'societe':this.societe}
      
      if(this.client!==null)
        data.query={...data.query,'client.nom':this.client.nom}
      
      if(this.status!=='')
        data.query={...data.query,'status':this.status}*/
      
      this.$store.dispatchAsync(this.$SHARED.services.user.list, data, true).then(ar=>{
        console.log('Users', ar)

        this.users = ar.docs
        this.pagination.count = ar.count
      })
    },
    addUser(){
      console.log('addUser')

      let u = this.user
      if(u.username === '' || u.email === '' || u.role ==null){
        this.$store.errorNotif('Veuillez remplir les champs')
        return
      }
      if(!/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/.test(u.email)){
        this.$store.errorNotif('Email invalid')
        return
      }
      this.isBusy = true

      this.$store.dispatchAsync(this.$SHARED.services.user.create, u).then(ar=>{
        console.log('user',ar);

        this.$modal.hide('user-modal')
        u.username = ''
        u.email = ''
        u.role = null
        this.$store.successNotif(this.$SHARED.messages.user.create_succeeded)
        this.getUsers()
      })
      .finally(()=>
          this.isBusy= false
      )
    },
    deleteUser(_id){
      console.log('deleteUser');
      if(confirm("Voulez-vous vraiment supprimer l'utilisateur ?") === true) {
        this.$store.dispatchAsync(this.$SHARED.services.user.delete, {_id}).then(ar => {
          console.log(ar);

          this.getUsers()
        })
      }
    },
    updateRole(_id){
      console.log('updateRole');

      let data = {
        _id,
        role: this.newRole
      }

      this.$store.dispatchAsync(this.$SHARED.services.user.update, data).then(ar=>{

        this.$store.successNotif('Rôle modifié avec succés')
        this.getUsers()
      }).finally(()=>{
        this.newRole= null
        this.edit= false
        this.editId= null
      })
    },
    generatePassword(user){
      console.log('generatePassword');

      if(confirm("Voulez-vous vraiment reintialiser le mot de passe de l'utilisateur ?") === true){
        this.$store.dispatchAsync(this.$SHARED.services.user.generetePassword, { user }).then(ar=>{
          console.log('generatePassword', ar);

          this.$store.successNotif("un mail contenant le nouveau mot de passe a été envoyé a l'utilisateur")
        })
      }
    }
  },
  mounted(){
    this.getUsers()
  }
}
</script>

<style scoped>
  thead{
    background: var(--color-3);
    color: grey;
  }
  thead th{
    font-family: var(--poppins) !important;
  }
  .edit .bi{
    font-size: 1.7rem;
    opacity: .5;
  }
  .edit .bi:hover{
    opacity: 1;
    cursor: pointer;
  }
</style>
