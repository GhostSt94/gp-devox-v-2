<template>
    <div class="container">

        <div class="row">
            <div class="col">
                <h2>Détails projets</h2>
            </div>
            <div class="col-auto">
                <comments-comp :projectId="projectId"/>
            </div>
            <div class="col text-end">
                <div v-if="!editing">
                    <button @click="editing=!editing" class="btn btn-secondary">
                        <i class="bi bi-pencil-square me-1"></i>
                        Modifier
                    </button>
                    <button :disabled="!$store.getters.IS_ADMIN" @click="deleteProject" class="btn btn-outline-danger ms-2">
                        <i class="bi bi-trash3-fill"></i>
                    </button>
                </div>
                <button v-else @click="()=>{editing=!editing;getProject()}" class="btn btn-secondary">
                    <i class="bi bi-x"></i>
                </button>
            </div>
            
        </div>

        <div class="container my-3">
            <div class="row justify-content-between">
                <div class="col-md-9 col-sm-12 pb-1">
                    <div class="card bg-light">
                        <div class="card-header text-center">INFO</div>
                        <div class="card-body row g-3">
                            <div class="col-md-12">
                                <label class="form-label">Societe</label>
                                <select v-model="project.societe" class="form-select form-select-sm" :disabled="!editing">
                                    <option v-for="soc in SOCIETE_LIST" :key="soc">{{soc}}</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Title</label>
                                <input  v-model="project.title" type="text" class="form-control" :disabled="!editing">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Client</label>
                                <input  v-if="!editing" v-model="project.client.nom" type="text" class="form-control" disabled>
                                <select v-else v-model="project.client" class="form-select">
                                    <option v-for="cl in clients" :key="cl._id" :value="cl">{{cl.nom}}</option>
                                </select>
                            </div>
                            <div class="col-md-12">
                                <label class="form-label">Description</label>
                                <textarea  v-model="project.description" class="form-control" placeholder="description here" :disabled="!editing"></textarea>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Date Debut</label>
                                <date-milli v-model="project.dateDebut" :disabled="!editing"/>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Date Fin</label>
                                <date-milli v-model="project.dateFin" :disabled="!editing"/>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Type Commande</label>
                                <select  v-model="project.typeCommande" class="form-select"  :disabled="!editing">
                                    <option v-for="cmd in TYPE_COMMANDE" :key="cmd">{{cmd}}</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Status</label>
                                <select  v-model="project.status" class="form-select" :disabled="!editing">
                                    <option v-for="st in ALL_STATUS" :key="st">{{st}}</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">Montant</label>
                                <input v-if="!editing" :value="$SHARED.utils.numberToMoneyFormat(project.montant)" type="text" class="form-control" disabled>
                                <input v-else class="form-control" v-model="project.montant" min="0" step="100" type="number" placeholder="10 000 DH">
                            </div>
                            <div v-show="project.typeCommande === 'Bon de commande'" class="col-md-6">
                                <label class="form-label">Garantie</label>
                                <input v-if="!editing" :value="$SHARED.utils.numberToMoneyFormat(project.garantie)" type="text" class="form-control" disabled>
                                <input v-else class="form-control" v-model="project.garantie" min="0" step="100" type="number" placeholder="10 000 DH">
                            </div>
                            <div v-show="editing" class="col-12 text-end">
                                <button @click='editProject' class="btn btn-secondary p-1">Modifier</button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card bg-light">
                        <div class="card-header text-center">
                            Attachements
                            <i @click="$modal.show('attachement-modal')" class="bi bi-file-plus float-end rounded-circle"></i>
                        </div>
                        <div class="card-body">
                            <div class="row p-1" v-for="attachement in projectAttachements" :key="attachement._id">
                                <div class="col-9 file-name">
                                    <span class="text-secondary h-underline pointer" @click="$store.openFile(attachement.file.link)">
                                        <i class="bi bi-file-earmark me-1"></i>
                                        {{attachement.file.name}}
                                    </span>
                                </div>
                                <div class="col-3">
                                    <button @click="deleteAttachement(attachement._id, attachement.file.path)" class="btn btn-close"></button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
            
        <factureComp :projectId='projectId'/>

        <!-- Attachement modal -->
        <modal name="attachement-modal" width="700px" style="border-radius: 20px;z-index:2000;" :adaptive="true"  height="auto" >
            <div class="container-fluid p-3 text-center modal-upload">
                <div class="container rounded bg-light shadow p-3">
                    <div class="row">
                        <svg xmlns="http://www.w3.org/2000/svg" height="70" fill="currentColor" class="bi bi-file-earmark" viewBox="0 0 16 16">
                            <path d="M14 4.5V14a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V2a2 2 0 0 1 2-2h5.5L14 4.5zm-3 0A1.5 1.5 0 0 1 9.5 3V1H4a1 1 0 0 0-1 1v12a1 1 0 0 0 1 1h8a1 1 0 0 0 1-1V4.5h-2z"/>
                        </svg>
                    </div>
                    <div class="d-flex justify-content-center py-2">
                        <h4>{{ fileInputText }}</h4>
                        <button class="btn btn-close ms-2" v-if="fileInputText!=='Veuillez selectionner un fichier'" @click="clearInput">
                        </button>
                    </div>
                    <div class="row justify-content-center mt-4">
                        <div class="col-md-4">
                            <input ref="attachementProject" hidden type="file" class="inputFile" @input="(e)=>fileInputText = e.target.files[0].name">
                            <button @click="$refs.attachementProject.click()" class="btn btn-secondary"><i class="bi bi-folder me-2"></i>Parcourir</button>
                        </div>
                        <div class="col-md-4">
                            <button @click="uploadAttachement" class="btn btn-primary"><i class="bi bi-upload me-2"></i>upload</button>
                        </div>
                    </div>
                </div>
            </div>
        </modal>

    </div>

</template>

<script>
import dateMilli from "@/components/generic/dateMilli.vue";
import factureComp from "./detailsComp/Factures.vue"
import CommentsComp from "./detailsComp/Comments.vue"
export default {
    name:'projectDetails',
    components:{ dateMilli, factureComp, CommentsComp },
    data(){
        return{
            projectId:this.$router.currentRoute.params.id,
            project:{},
            projectAttachements:[],
            clients:[],
            editing:false,
            fileInputText:'Veuillez selectionner un fichier'
        }
    },
    computed:{
        SOCIETE_LIST(){return this.$store.getters.getSocieties},
        ALL_STATUS(){return this.$store.getters.getAllStatus},
        TYPE_COMMANDE(){return this.$store.getters.getTypeCommand},
    },
    methods:{
        getProject(){
            console.log('getProject');

            let data={
                query:{ _id: this.projectId },
                options:{}
            }

            this.$store.dispatchAsync(this.$SHARED.services.project.list, data).then(ar=>{
                console.log(ar);

                if(ar.length===0){
                    this.$store.errorNotif('Project not found')
                    this.redirect()
                }
                this.project= ar[0]
            })
        },
        editProject(){
            console.log('editProject');

            this.$store.dispatchAsync(this.$SHARED.services.project.update, this.project).then(ar=>{
                console.log(ar);

                this.$store.successNotif(this.$SHARED.messages.project.update_succeeded)
                this.editing= false
            })
        },
        deleteProject(){
            console.log('deleteProject');

            this.$store.dispatchAsync(this.$SHARED.services.project.delete, { _id: this.project._id }).then(ar=>{
                console.log(ar);

                this.$store.successNotif(this.$SHARED.messages.project.delete_succeeded)
                this.redirect()
            })
        },
        uploadAttachement(){
            console.log('uploadProjectAttachement');

            let files=this.$refs.attachementProject.files
            if(files.length !== 0 ){
                
                let formData = new FormData()
                formData.append('file', files[0])
                formData.append('id_project', this.project._id)

                this.$store.uploadFile(this.$SHARED.services.upload, formData).then(ar=>{
                    console.log(ar)

                    this.$store.successNotif(this.$SHARED.messages.upload.upload_succeeded)

                    this.$refs.attachementProject.value= null
                    this.fileInputText = 'Veuillez selectionner un fichier'
                    this.getProjectAttachements()
                    this.$modal.hide('attachement-modal')
                })
            }else{
                this.$store.errorNotif('veuillez selectionner un fichier')
            }
        },
        getProjectAttachements(){
            console.log('getProjectAttachement');

            let data= {
                query:{ id_project: this.projectId },
                options:{}
            }

            this.$store.dispatchAsync(this.$SHARED.services.attachement.list, data).then(ar=>{
                console.log('attachements',ar);

                this.projectAttachements= ar
            })
        },
        deleteAttachement(_id, path){
            console.log('getProjectAttachement');

            this.$store.dispatchAsync(this.$SHARED.services.attachement.delete, {_id, path}).then(ar=>{
                console.log('attachements',ar);
                this.$store.successNotif('Attachements supprimé')
                this.getProjectAttachements()
            })
        },
        getClients(){
            console.log('getClients');
        
            this.$store.dispatchAsync(this.$SHARED.services.client.list).then(ar=>{
                console.log('clients', ar);

                this.clients= ar
            })
        },
        clearInput(){
            this.$refs.attachementProject.value = null;
            this.fileInputText = 'Veuillez selectionner un fichier'
        },
        redirect(){
            console.log('redirect');
            this.$router.push({name: 'projects'})
        }
    },
    mounted(){
      this.getProject()
      this.getProjectAttachements()
      this.getClients()
      if(!this.projectId) this.redirect()
    }

}
</script>

<style scoped>
.card-header{
    background: var(--color-3);
}
input, select{
    font-weight: bold;
}
.bi-file-plus{
    padding: 3px 6px;
}
.bi-file-plus:hover{
    background: var(--grey);
    cursor: pointer;
}
label{
    color: grey;
}
.file-name span:hover{
    color: var(--dark) !important;
}
.modal-upload{
    background: var(--color-3);
}
.btn-close{
  padding: 0;
  font-size: .8rem;
}
</style>