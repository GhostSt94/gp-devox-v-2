<template>
    <div>
        <button @click="$modal.show('comment-modal')" class="btn btn-light position-relative com">
            <i class="bi bi-chat-left-dots px-1"></i>
            <span v-show="comments.length > 0" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
                {{ comments.length }}
                <span class="visually-hidden">Comments</span>
            </span>
        </button>

  <!-- Modal Comment -->
        <modal name="comment-modal" width="700px" style="border-radius: 20px;z-index: 2000;" :adaptive="true"  height="auto" >
          <div class="row d-flex justify-content-center">
            <div class="col-12">

              <div class="card shadow-0 border" style="background-color: #f0f2f5;">
                <div class="card-body p-4">
                  <div class="row g-2">
                    <div class="col-9">
                      <textarea v-model="comment" class="form-control" placeholder="your comment here"></textarea>
                    </div>
                    <div class="col-3">
                      <button @click="addComment" class="btn btn-secondary w-100 h-100">Ajouter commentaire</button>
                    </div>
                  </div>
                  <hr class="mb-3">

                  <div class="container comments">
                    <div v-for="cmt in comments" :key="cmt._id" class="card mb-2 shadow">
                      <div class="card-body">
                        <p>{{ cmt.comment}}</p>

                        <div class="d-flex justify-content-between">
                          <div class="d-flex flex-row align-items-center">
                            <img src="https://mdbcdn.b-cdn.net/img/Photos/Avatars/img%20(32).webp" alt="avatar" width="25"
                                 height="25" />
                            <p class="small mb-0 ms-2 me-2">{{ cmt.user.username}}</p>
                            <p class="small text-muted mb-0">| {{ $SHARED.utils.getDate(cmt.tmsp)}}</p>
                          </div>
                          <div class="d-flex flex-row align-items-center">
                            <p v-show="IS_ADMIN || CURRENT_USER._id === cmt.user._id" @click="deleteComment(cmt._id)" class="small text-muted mb-0 h-underline pointer">Supprimer</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
        </div>
        </modal>
    </div>
</template>

<script>
export default {
    name:'Comments',
    props:{
        projectId:{
            required: true,
            type: String
        }
    },
    data(){
        return{
            comments:[],
            comment:'',
        }
    },
    computed:{
      CURRENT_USER(){ return this.$store.state.current_user },
      IS_ADMIN(){ return this.CURRENT_USER.role === 'Administrateur'}
    },
    methods:{
        getComments(){
            console.log('getComments');

            let data={
            query:{ id_project: this.projectId },
            options:{}
            }

            this.$store.dispatchAsync(this.$SHARED.services.comment.list, data).then(ar=>{
            console.log('comments',ar);

            this.comments= ar
            })
        },
        addComment() {
            console.log('addFacture');

            if (this.comment === '') {
                this.$store.errorNotif('input vide')
                return
            }

            let data = {
                comment: this.comment,
                tmsp: this.$SHARED.utils.getMilliseconds(),
                id_project: this.projectId,
                user: this.CURRENT_USER

            }

            this.$store.dispatchAsync(this.$SHARED.services.comment.create, data).then(ar => {
                console.log(ar);

                this.$store.successNotif(this.$SHARED.messages.comment.create_succeeded)
                this.comment = ''

                this.getComments()
            })
        },
        deleteComment(_id){
            console.log('deleteComments');

            this.$store.dispatchAsync(this.$SHARED.services.comment.delete, {_id}).then(ar=>{
            console.log('deleteComments',ar);

            this.getComments()
            })
        },
    },
    mounted(){
      this.getComments()
    }
}
</script>

<style scoped>
.comments{
  max-height: 350px;
  overflow-y: auto;
}
</style>