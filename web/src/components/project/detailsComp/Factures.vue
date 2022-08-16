<template>
    <div class="container">
        <div class="row m-2 py-3">
            <div class="col">
                <h2>Factures</h2>
            </div>
            <div class="col text-end">
                <button @click="$modal.show('facture-modal')" class="btn btn-secondary">
                    <i class="bi bi-plus"></i>
                </button>
            </div>
        </div>
        
        <div class="container">
            <table class="table">
                <thead>
                    <tr>
                        <th>N°</th>
                        <th>Date Facture</th>
                        <th>Date Paiement</th>
                        <th>Montant</th>
                        <th>Status</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody class="bg-light">
                    <tr v-for="f in factures" :key="f._id">
                        <td>{{f.num}}</td>
                        <td>{{$SHARED.utils.getDate(f.dateFacture, 'DD/MM/YYYY')}}</td>
                        <td>{{$SHARED.utils.getDate(f.datePaiement, 'DD/MM/YYYY')}}</td>
                        <td class="text-success">{{$SHARED.utils.numberToMoneyFormat(f.montant)}}</td>
                        <td>
                            <select v-if="factureUpdateId === f._id"></select>
                            <div v-else>
                              <i class="bi bi-circle-fill me-1"></i>
                              {{f.status}}
                            </div>
                        </td>
                        <td>
                          <div  v-if="factureUpdateId === f._id">
                            <i class="bi bi-check"></i>
                            <i class="bi bi-x"></i>
                          </div>
                          <div v-else class="dropdown">
                              <button class="btn" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                  <i class="bi bi-three-dots text-end"></i>
                              </button>
                              <ul class="dropdown-menu">
<!--                                  TODO: facture modify status-->
                                  <li @click="factureUpdateId= f._id"><span class="dropdown-item">Modifier status</span></li>
                                  <li @click="$store.openFile(f.attachements[0].file.link)"><span :class="f.attachements.length === 0 && 'disabled'" class="dropdown-item">Voir fichier</span></li>
                                  <div class="dropdown-divider"></div>
                                  <li @click="deleteFacture(f)"><span class="dropdown-item text-danger">Supprimer</span></li>
                              </ul>
                          </div>
                        </td>
                    </tr>
                </tbody>
            </table>
            <div class="container text-center pb-4">
                <pagination-2 v-bind="pagination" @setPage='setPage'/>
            </div>
        </div>

        <!-- Facture modal -->
        <modal name="facture-modal" width="700px" style="border-radius: 20px;z-index:2000" :adaptive="true"  height="auto" >
            <div class="container p-3">
                <h2 class="secondary">Ajouter facture</h2>
                <hr>
                <div class="row g-3">
                    <div class="col-md-12">
                        <label class="form-label required">N°</label>
                        <input v-model="facture.num" type="text" class="form-control">
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Date Facture</label>
                        <date-milli v-model="facture.dateFacture"/>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Date Paiement</label>
                        <date-milli v-model="facture.datePaiement"/>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Status</label>
                        <select v-model="facture.status" class="form-select">
                            <option v-for="st in FACTURE_STATUS" :key="st">{{st}}</option>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">Montant (DH)</label>
                        <input class="form-control" v-model="facture.montant" min="0" step="100" type="number" placeholder="10 000 DH">
                    </div>
                    <div class="col-md-12">
                        <label class="form-label">Fichier</label><br>
                        <div class="row p-1">
                            <div class="col-auto ms-2">
                                <input hidden class="form-control" type="file" ref="attachementFacture" @input="(e)=>fileInputText=e.target.files[0].name">
                                <button @click="$refs.attachementFacture.click()" class="btn btn-upload">Parcourir</button>
                            </div>
                            <div class="col-auto p-1">
                                <span>{{ fileInputText}}</span>
                                <button v-if="fileInputText!=='Veuillez selectionner un fichier'" @click="clearInput" type="button" class="btn-close m-1"></button>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-12 text-end">
                        <button @click="addFacture" class="btn btn-secondary">Ajouter</button>
                    </div>
                </div>
            </div>
        </modal>
    </div>
</template>

<script>
import pagination2 from "@/components/generic/pagination2.vue";
import dateMilli from "@/components/generic/dateMilli.vue";
export default {
    name: 'factures',
    components:{ pagination2, dateMilli},
    props: {
        projectId:{
            required: true,
            type: String
        }
    },
    computed:{
        FACTURE_STATUS(){ return this.$store.state.factureStatus}
    },
    data(){
        return{
            factures:[],
            facture:{
                num:'',
                dateFacture: null,
                datePaiement: null,
                status: null,
                montant: null,
            },
            pagination: {
                page: 1,
                limit: 8,
                count: 0
            },
            fileInputText:'Veuillez selectionner un fichier',
          factureUpdateId: null
        }
    },
    methods:{
        getFactures(){
            console.log('getFactures');

            let data={
                query:{ id_project: this.projectId },
                options:{
                    page: this.pagination.page,
                    limit: this.pagination.limit,
                    withCount: true
                }
            }

            this.$store.dispatchAsync(this.$SHARED.services.facture.list, data).then(ar=>{
                console.log('Factures',ar);

                this.factures = ar[0].docs
                this.pagination.count = ar[0].countFactures[0].count
            })
        },
        addFacture(){
            console.log('addFacture');

            let f=this.facture
            if(f.num===''){
                this.$store.errorNotif('Veuillez remplir les champs obligatoire *')
                return
            }
            this.facture.id_project=this.projectId
            
            var fileInp= this.$refs.attachementFacture

            this.$store.dispatchAsync(this.$SHARED.services.facture.create, this.facture).then(ar=>{
                console.log('addFacture', ar);

                if(fileInp.files.length !== 0 ){
                    
                    let formData = new FormData()
                    formData.append('file', fileInp.files[0])
                    formData.append('id_facture', ar._id)

                    this.$store.uploadFile(this.$SHARED.services.upload, formData).then(ar=>{
                        console.log('uploadFile', ar);

                        this.$store.successNotif(this.$SHARED.messages.facture.create_succeeded)
                        this.$modal.hide('facture-modal')
                        this.getFactures()
                    })
                }
            })
        },
        deleteFacture(facture){
            console.log('deleteFacture');

            let data = {
                _id: facture._id
            }

            if(facture.attachements[0]){
                let att = facture.attachements[0]
                data.attachement = {
                    _id: att._id,
                    path: att.file.path
                }
            }

            this.$store.dispatchAsync(this.$SHARED.services.facture.delete, data).then(ar=>{
                console.log(ar);

                this.$store.successNotif(this.$SHARED.messages.facture.delete_succeeded)
                this.getFactures()
            })
        },
        //
        setPage(page){
            console.log("page: "+page)

            this.pagination.page=page
            this.getFactures()
        },
        clearInput(){
            this.$refs.attachementFacture.value = null;
            this.fileInputText = 'Veuillez selectionner un fichier'
        },
    },
    mounted(){
        this.getFactures()
    }
}
</script>

<style scoped>
thead{
    background: var(--color-3);
}
th{
    font-family: var(--poppins) !important;
    font-weight: lighter;
}
tbody tr td:first-child{
    color: var(--blue);
}
.dropdown button:hover{
    border-color: transparent;
}
.bi-circle-fill{
    font-size: .7rem;
    color: var(--color-1);
}
.btn-upload{
    background: var(--color-2);
    border:1px solid var(--color-2);
    color: var(--light);
}
.btn-upload:hover{
    background: transparent;
    border-color: var(--color-2);
    color: var(--color-2);
}
@media screen and (max-width: 700px) {
        table tr {display: flex;flex-wrap: wrap;border-bottom: 2px solid grey ;padding: 5px;}
        td, th{border: none;}
    }
</style>