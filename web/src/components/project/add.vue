<template>
    <div class="container pb-5">

        <div class="row mb-2 header-content">
            <div class="col">
                <h2>Ajouter projet</h2>
            </div>
        </div>
            
        <div class="row g-3">
            <div class="col-md-12">
                <label for="select1" class="form-label required">Societe</label>
                <select v-model="project.societe" id="select1" class="form-select form-select-sm">
                    <option v-for="soc in SOCIETE_LIST" :key="soc">{{soc}}</option>
                </select>
            </div>
            <div class="col-md-6">
                <label for="inp1" class="form-label required">Title</label>
                <input  v-model="project.title" type="text" class="form-control" placeholder="Title project" id="inp1">
            </div>
            <div class="col-md-6">
                <label for="select2" class="form-label required">Client</label>
                <select  v-model="project.client" id="select2" class="form-select">
                    <option v-for="cl in clients" :key="cl._id" :value="cl">{{cl.nom}}</option>
                </select>
            </div>
            <div class="col-md-12">
                <label for="txtarea" class="form-label">Description</label>
                <textarea  v-model="project.description" class="form-control" placeholder="description here" id="txtarea"></textarea>
            </div>
            <div class="col-md-6">
                <label class="form-label">Date Debut</label>
                <date-milli v-model="project.dateDebut" />
            </div>
            <div class="col-md-6">
                <label class="form-label">Date Fin</label>
                <date-milli v-model="project.dateFin" />
            </div>
            <div class="col-md-6">
                <label for="select3" class="form-label">Type Commande</label>
                <select  v-model="project.typeCommande" id="select3" class="form-select">
                    <option v-for="cmd in TYPE_COMMANDE" :key="cmd">{{cmd}}</option>
                </select>
            </div>
            <div class="col-md-6">
                <label for="select4" class="form-label">Status</label>
                <select  v-model="project.status" id="select4" class="form-select">
                    <option v-for="st in ALL_STATUS" :key="st">{{st}}</option>
                </select>
            </div>
            <div class="col-md-6">
                <label for="mt" class="form-label">Montant (DH)</label>
                <input class="form-control" v-model="project.montant" id="mt" min="0" step="100" type="number" placeholder="10 000 DH">
            </div>
            <div v-show="project.typeCommande === 'Bon de commande'" class="col-md-6">
                <label for="gr" class="form-label">Garantie (DH)</label>
                <input class="form-control" v-model="project.garantie" id="gr" min="0" step="100" type="number" placeholder="10 000 DH">
            </div>
            <div class="col-12 text-end">
                <button @click='addProject' class="btn btn-secondary p-1">Ajouter</button>
            </div>
        </div>
    </div>
</template>

<script>
import dateMilli from "@/components/generic/dateMilli.vue";
export default {
    name:'addProject',
    components:{ dateMilli},
    data(){
        return{
            clients:[],
            project:{
                societe:'',
                title:'',
                client:{},
                dateDebut:null,
                dateFin:null,
                status:'',
                typeCommande:'',
                montant:0,
                garantie:0,
            }
        }
    },
    computed:{
        SOCIETE_LIST(){ return this.$store.getters.getSocieties },
        ALL_STATUS(){ return this.$store.getters.getAllStatus },
        TYPE_COMMANDE(){ return this.$store.getters.getTypeCommand },
    },
    methods:{
        addProject(){
            console.log('addProject');
            
            let p=this.project
            if(p.societe===''||p.title===''||p.client===''){

                this.$store.errorNotif('Veuillez remplir les champs obligatoire *')
                return
            }

            this.$store.dispatchAsync(this.$SHARED.services.project.create, this.project).then(ar=>{
                console.log(ar);

                this.$store.successNotif(this.$SHARED.messages.project.create_succeeded)
                this.$router.push({name: 'projects'})
            })
        },
        getClients(){
          console.log('getClients');

          this.$store.dispatchAsync(this.$SHARED.services.client.list).then(ar=>{
            this.clients= ar
          })
        }
    },
  mounted() {
      this.getClients()
  }
}
</script>

<style scoped>

</style>