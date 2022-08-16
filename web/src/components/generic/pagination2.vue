<template>
    <nav v-show="totalPages > 1">
        <div class="btn-group me-2" role="group">
            <button  @click="changePage(1)" :class="{'hide' : page === 1}" class="btn btn-pagination me-2" aria-label="Previous">
                <span aria-hidden="true">&laquo;</span>
            </button>

            <button v-for="p in pages" :key="p" @click="changePage(p)" :class="{'disabled' : page === p}" class="btn btn-pagination">{{ p }}</button>
           
            <button  @click="changePage(totalPages)"  :class="{'hide' : page === totalPages}" class="btn btn-pagination ms-2" aria-label="Next">
                <span aria-hidden="true">&raquo;</span>
            </button>
        </div>
    </nav>
</template>

<script>
export default {
  name:'pagination',
  props: {
    page: {
      type: Number,
      default: 1
    },
    count: {
      type: Number,
      default: 0
    },
    limit: {
      type: Number,
      default: 5
    },
    pageRange: {
      type: Number,
      default: 2
    }
  },
  computed:{
    pages() {
      let pages = [];
      for (let i = this.rangeStart; i <= this.rangeEnd; i++) {
        pages.push(i);
      }
      return pages;
    },
    rangeStart() {
      let start = this.page - this.pageRange;
      return start > 0 ? start : 1;
    },
    rangeEnd() {
      let end = (this.page + this.pageRange) + 1 ;
      return end < this.totalPages ? end : this.totalPages;
    },
    totalPages() {
      return Math.ceil(this.count / this.limit);
    }
  },
  methods:{
    changePage(page,valid = true) {
      if(!valid){
        return
      }
      this.$emit("setPage", page);
    }
  }
}
</script>

<style scoped>
.hide{
    visibility: hidden;
}

</style>
