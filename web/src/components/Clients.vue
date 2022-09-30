<template>

  <div class="container">
    <div class="row header-content">
      <div class="col-md-4 col-sm-12">
        <h2>Liste des Clients</h2>
      </div>
      <div class="col">
        <input @input="search" v-model="searchInput" type="text" class="form-control" placeholder="search">
      </div>
      <div class="col text-end">
        <button v-show="$store.getters.IS_ADMIN" @click="$modal.show('client-modal')" class="btn btn-primary">
          <i class="bi bi-plus"></i>
            Nouveau Client
        </button>
      </div>
    </div>

    
    <div class="container py-4">

      <div class="row g-3 g-md-3 g-sm-2">
        <div v-if="clients.length===0" class="text-center text-muted">
          <h6>Aucun client trouver</h6>
        </div>
        <div v-else v-for="cl in clients" :key="cl._id" class="col-sm-4 col-md-4 col-lg-3">
          <div class="h-100 p-2 border rounded bg-light d-flex justify-content-between client">
            <h6 class="m-1">{{ cl.nom }}</h6>
            <div class="dropdown">
              <button class="btn" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                <i class="bi bi-three-dots text-end"></i>
              </button>
              <ul class="dropdown-menu">
                <li @click="()=>{$modal.show('client-details-modal');selectedClient=cl}"><span class="dropdown-item">Détails</span></li>
                <div class="dropdown-divider"></div>
                <li><span @click="deleteClient(cl._id)" :class="{'disabled text-light': !$store.getters.IS_ADMIN}" class="dropdown-item text-danger">Supprimer</span></li>
              </ul>
            </div>
          </div>
        </div>
      </div>
      <div class="w-100 pt-3 d-flex justify-content-center">
        <pagination-2  v-bind="pagination" @setPage="setPage" />
      </div>
      
    </div>

    <!-- modal -->
        <modal name="client-modal" width="700px" style="border-radius: 20px;z-index:2000" :adaptive="true"  height="auto" >
            <div class="container p-3">
                <h2 class="secondary">Ajouter Client</h2>
                <hr>
                <div class="row g-3">
                    <div class="col-md-16">
                        <label class="form-label required">Nom</label>
                        <input v-model="client.nom" type="text" class="form-control">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Ville</label>
                        <input v-model="client.ville" type="text" class="form-control">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Tel</label>
                        <input v-model="client.tel" type="phone" class="form-control">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Email</label>
                        <input v-model="client.email" type="mail" class="form-control">
                    </div>
                    <div class="col-md-12 text-end">
                        <button @click="addClient" class="btn btn-secondary">Ajouter</button>
                    </div>
                </div>
            </div>
        </modal>

        <modal name="client-details-modal" width="500px" style="border-radius: 20px;z-index:2000" :adaptive="true"  height="auto" >
              <div class="card">
                <div class="card-header text-center">
                  {{selectedClient.nom}}
                </div>
                <div class="card-body details">
                  <p v-show="selectedClient.tel"><span>Tel: </span>{{selectedClient.tel}}</p>
                  <p v-show="selectedClient.email"><span>Email: </span><a class="h-underline" :href="'mailto:'+selectedClient.email">{{selectedClient.email}}</a></p>
                  <p  v-show="selectedClient.ville"><span>ville: </span>{{selectedClient.ville}}</p>
                </div>
              </div>
        </modal>
  </div>

</template>

<script>
import Pagination2 from './generic/pagination2.vue'

export default {
  name: 'clients',
  components: {
    Pagination2
  },
  data(){
    return{
      clients:[],
      pagination: {
        page: 1,
        limit: 10,
        count: 0
      },
      client:{
        nom:'',
        email:'',
        ville:'',
        tel:''
      },
      selectedClient:{},
      searchInput:'',
      timer: null
    }
  },
  methods: {
    setPage(page){
      console.log("page: "+page)

      this.pagination.page=page
      this.getClients()
    },
    getClients(){
      console.log('getClients');

      let query ={}
      if(this.searchInput !== '')
        query.nom={$regex: this.searchInput, $options: 'i'}

      let data={
        query,
        options:{
          page: this.pagination.page,
          limit: this.pagination.limit,
          withCount: true
        }
      }

      this.$store.dispatchAsync(this.$SHARED.services.client.list, data, true).then(ar=>{
          console.log('clients', ar);

          this.clients = ar.docs
          this.pagination.count = ar.count
      })
    },
    addClient(){
      console.log('addClient');

      let cl = this.client
      if(cl.nom ===''){
        this.$store.errorNotif('Veuillez remplire les champs obligatoire *')
        return
      }
      if(cl.email !== ''){
        if(!this.$SHARED.utils.checkIfMail(cl.email)){
          this.$store.errorNotif('Format email invalid')
          return
        }
      }

      this.$store.dispatchAsync(this.$SHARED.services.client.create, cl).then(ar =>{
        console.log(ar);

        this.$modal.hide('client-modal')
        this.getClients()
      })
    },
    deleteClient(_id){
      console.log('deleteClient');
      
      this.$store.dispatchAsync(this.$SHARED.services.client.delete, { _id }).then(ar=>{
        console.log(ar);

        this.getClients()
      })
    },
    search(){
      if (this.timer) {
        clearTimeout(this.timer);
        this.timer = null;
      }
      this.timer = setTimeout(() => {
          this.getClients()
      }, 1000);
    }
  },
  mounted(){
    this.getClients()
  }
}
</script>

<style scoped>
/* .client:hover{
  box-shadow: 1px 1px 2px var(--color-2);
  color: var(--color-1) !important;
  cursor: pointer;
} */
.filter{
  border: 1px solid var(--blue);
  color: var(--blue);
  font-family: var(--poppins);
  transition: .3s ease-in;
}
.details span{
  font-size: .9rem;
  color: grey;
}

</style>
