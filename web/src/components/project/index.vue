<template>

  <div class="container" id="project-content">
    <div class="row header-content">
      <div class="col">
        <h2>Tous les projets</h2>
      </div>
      <div class="col text-end">
        <span @click="filter = !filter" class="pe-5 h-underline pointer">Filter</span>
        <button @click="$router.push({name: 'addProject'})" class="btn btn-primary">
            <i class="plus icon"></i>
            New Project
        </button>
      </div>
    </div>

    
    <div class="container">
      <div class="row my-2 p-1 rounded shadow-sm filter g-2" :class="{'d-none' : !filter}">
        <div class="col">
            <label for="soc" class="form-label">Societe</label>
            <select v-model="societe" class="form-select form-select-sm" id="soc">
              <option></option>
              <option v-for="soc in SOCIETE_LIST" :key="soc">{{ soc }}</option>
            </select>
        </div>
        <div class="col">
            <label for="cl" class="form-label">Client</label>
            <select v-model="client" class="form-select form-select-sm" id="cl">
              <option></option>
              <option v-for="cl in clients" :key="cl._id" :value="cl">{{ cl.nom }}</option>
            </select>
        </div>
        <div class="col">
            <label for="st" class="form-label">Societe</label>
            <select v-model="status" class="form-select form-select-sm" id="st">
              <option></option>
              <option v-for="st in ALL_STATUS" :key="st">{{ st }}</option>
            </select>
        </div>
        <div class="col-12 text-end">
          <button  @click="getProjects" class="btn btn-secondary">Search</button>
          <span  @click="resetFilter" class="ps-2 h-underline pointer">reset</span>
        </div>
      </div>
    </div>

    <div class="row justify-content-center" style="font-size:.8rem">
      <div class="col text-center">
        <hr class="Devox d-inline-block" style="width:15px">
        <span class="me-3">Devox</span> 
        <hr class="ALFAY d-inline-block" style="width:15px">
        <span class="me-3">Alfay</span>
        <hr class="View Times d-inline-block" style="width:15px">
        <span class="me-3">View Times</span>
      </div>
    </div>
    
    <div class="container py-4">

      <div class="row g-5 g-md-3 g-sm-2">
        <div v-for="p in projects" :key="p._id" class="col-sm-12 col-md-4 col-lg-4">
          <div class="h-100 border rounded pt-1 pb-2 d-flex justify-content-between flex-column card">
            
            <div class="row project-header ps-2 mb-2">
              <div class="col"><span>{{ p.status }}</span></div>
              <div class="col text-end">
                
                <div class="dropdown">
                  <button class="btn" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                    <i class="bi bi-three-dots text-end"></i>
                  </button>
                  <ul class="dropdown-menu">
                    <li  @click="$router.push(`/private/project/${p._id}`)"><span class="dropdown-item">Voir Détails</span></li>
                    <div class="dropdown-divider"></div>
                    <li @click="deleteProject(p._id)"><span class="dropdown-item text-danger">Supprimer</span></li>
                  </ul>
                </div>
              </div>
            </div>

            <div class="row project-body px-3">
              <div class='d-flex justify-content-center flex-column'>
                <h3>{{ p.title }}</h3>
                <p>{{ p.client.nom }}</p>
              </div>
            </div>
            <hr :class="p.societe">
            <div class="d-flex justify-content-between date px-1">
              <small>
                {{ p.dateDebut ? $SHARED.utils.getDateOnly(p.dateDebut) : bars }}
              </small>
              <small>
                {{ p.dateFin ? $SHARED.utils.getDateOnly(p.dateFin) : bars }}
              </small>
            </div>
          </div>
        </div>
      </div>
      <div class="w-100 pt-2 d-flex justify-content-center">
        <pagination-2  v-bind="pagination" @setPage="setPage" />
      </div>
      
    </div>
  </div>

</template>

<script>
import tableType1 from '@/components/generic/tableType1'
import Pagination2 from '../generic/pagination2.vue'

export default {
  name: 'home',
  components: {
    tableType1,
    Pagination2
  },
  data(){
    return{
      projects:[],
      pagination: {
        page: 1,
        limit: 8,
        count: 0
      },
      clients: [],
      filter:false,
      societe:'',
      client:null,
      status:'',
      bars:'----'
    }
  },
  computed:{
    SOCIETE_LIST(){ return this.$store.getters.getSocieties },
    ALL_STATUS(){ return this.$store.getters.getAllStatus }
  },
  methods: {
    setPage(page){
      console.log("page: "+page)

      this.pagination.page=page
      this.getProjects()
    },
    resetFilter(){
      this.societe= ''
      this.client= null
      this.status= ''

      this.getProjects()
    },
    getProjects(){
      console.log('getProjects');
      
      let data={
        query:{},
        options:{
          page: this.pagination.page,
          limit: this.pagination.limit,
          withCount: true,
          sort: {'tmsp': -1}
        }
      }

      if(this.societe!=='')
        data.query={...data.query,'societe':this.societe}
      
      if(this.client!==null)
        data.query={...data.query,'client.nom':this.client.nom}
      
      if(this.status!=='')
        data.query={...data.query,'status':this.status}
      
      this.$store.dispatchAsync(this.$SHARED.services.project.list, data, true).then(ar=>{
        console.log('projects', ar)

        this.projects = ar.docs
        this.pagination.count = ar.count
      })
    },
    deleteProject(_id){
      console.log('deleteProject');
      
      this.$store.dispatchAsync(this.$SHARED.services.project.delete, { _id }).then(ar=>{
        console.log(ar);

        this.getProjects()
      })
    },
    getClients(){
      console.log('getClients');

      this.$store.dispatchAsync(this.$SHARED.services.client.list).then(ar=>{
        this.clients= ar
      })
    },
  },
  mounted(){
    this.getProjects()
  }
}
</script>

<style scoped>
.card{
  background: var(--light);
}
.filter{
  border: 1px solid var(--blue);
  color: var(--blue);
  font-family: var(--poppins);
  transition: .3s ease-in;
}
hr{
  border: none;
  height: 3px;
  opacity: 1;
}
hr.Devox{ background: var(--color-1) }
hr.ALFAY{ background: var(--blue) }
hr.View.Times{ background: var(--green) }

.project-header span,.date{
  font-size: 1rem;
  font-family: var(--lato);
}
.project-body{
  min-height: 100px;
  white-space: pre-wrap;
  word-break: break-all;
}
.project-body h3{
  font-family: var(--poppins);
  font-weight: bolder;
}
.project-body p{
  color: grey;
  font-size: .9rem;
}
</style>
