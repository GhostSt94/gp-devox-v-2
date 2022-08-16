<template>
  <div class="container">
    <div class="row justify-content-around stats">
      <div class="col-5 text-center border bg-light p-2 rounded">
        <h3 class="primary">{{ projectCount}}</h3>
        <h5>Projects</h5>
      </div>
      <div class="col-5 text-center border bg-light p-2 rounded">
        <h3 class="secondary">{{ factureCount}}</h3>
        <h5>Facture</h5>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'home',
  data(){
    return{
      projectCount: 0,
      factureCount: 0
    }
  },
  methods:{
    getProjectStats(){
      console.log('getProjectStats');

      this.$store.dispatchAsync(this.$SHARED.services.project.stats, {}).then(ar=>{
        this.projectCount = ar.count
      })
    },
    getFactureStats(){
      console.log('getFactureStats');

      this.$store.dispatchAsync(this.$SHARED.services.facture.stats, {}).then(ar=>{
        this.factureCount = ar.count
      })
    }
  },
  mounted(){
    this.getProjectStats()
    this.getFactureStats()
  }
}
</script>

<style scoped>
.stats h5{
  font-family: var(--poppins) ;
}
</style>