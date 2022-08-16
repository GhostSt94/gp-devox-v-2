<template>
  <div class="">
    <div class="flex items-center justify-between">
      <div class="flex items-center  basic-pagination">
        <div class="basic-pagination-label">Current Page:</div>
        <div class="pages">
          <span class="currentPage">{{page}}</span> / {{totalPages}}
        </div>
        <div v-show="totalPages>1" class="btns">
          <button class="btn" :disabled="page === 1" @click="changePage(1)"><i class="large angle double left icon"></i></button>
          <button class="btn" :disabled="!hasPrev" @click="changePage(page - 1)"><i class="large angle left icon"></i></button>
          <button class="btn" :disabled="!hasNext" @click="changePage(page + 1)"><i class="large angle right icon"></i></button>
          <button class="btn" :disabled="page === totalPages" @click="changePage(totalPages)">
            <i class="large angle double right icon"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
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
  methods:{
    changePage(page,valid = true) {
      if(!valid){
        return
      }
      this.$emit("setPage", page);
    },
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
    },
    hasPrev() {
      return this.page > 1;
    },
    hasNext() {
      return this.page < this.totalPages;
    }
  }
}
</script>

<style scoped>
.simple-pagination{
  --tw-text-opacity: 1;
  color: rgba(107,114,128,var(--tw-text-opacity));
  font-weight: 500;
  font-size: .875rem;
  line-height: 1.25rem;
  padding-left: .25rem;
  padding-top: 1rem;
  border-color: transparent;
  border-top-width: 2px;
  align-items: center;
  display: inline-flex;

}
.simple-pagination.disabled{
  opacity: 0.5;
  cursor:default

}
.simple-pagination.disabled:hover{
  opacity: 0.5;
  cursor:default;
  --tw-text-opacity: 1;
  color: rgba(107,114,128,var(--tw-text-opacity));

}
.simple-pagination:hover {
  --tw-text-opacity: 1;
  color: rgba(55,65,81,var(--tw-text-opacity));

}
.simple-pagination.next > svg{
  --tw-text-opacity: 1;
  color: rgba(156,163,175,var(--tw-text-opacity));
  width: 1.25rem;
  height: 1.25rem;
  margin-left: .5rem;
}
.simple-pagination.prev > svg{
  --tw-text-opacity: 1;
  color: rgba(156,163,175,var(--tw-text-opacity));
  width: 1.25rem;
  height: 1.25rem;
  margin-right: .5rem;
}
.basic-pagination{
  display: flex;
  align-items: center;
  line-height: 1.2;
}
.basic-pagination > .basic-pagination-label{
  font-size: 1.1em;
  line-height: 1.2;
  font-weight: 500;
  margin-right: 0.8em;
  color: var(--main-color-blue);
}
.basic-pagination >.btns{
  display: flex;
}
.basic-pagination >.pages>.currentPage{
  background-color:#E6F7FF ;
  padding:0em 0.5em;
  border-radius: 5px;
}
.basic-pagination >.pages{
  color: var(--main-color-blue);
  font-weight: 500;
}
.basic-pagination >.btns>.btn{
  margin: 0em 0.3em !important;
  padding: 0em;
  line-height: 1;
  border:none;
  background-color: transparent;
  cursor: pointer ;
}
.srp-btn{
  background-color: var(--main-color-blue);
  border: none;
  padding: 5px 5px;
  border-radius: 3px;
  cursor:pointer;
  line-height: 0.23;
}
.pagination-field:focus{
  outline: none;
}
.pagination-field  > label{
  font-size: 1.1em;
  line-height: 1.2;
  font-weight: 500;
  margin-right: 0.8em;
  color: var(--main-color-blue);
}
.pagination-field > select,input,textarea{
  color: var(--main-color-blue) !important;
  font-weight: 500;
  border-radius: 0.5em !important;
  border: 1px solid var(--main-color-blue) !important;
  font-family: 'Poppins', sans-serif !important;
  padding: 5px 6px !important;
}
.btns{
  margin-left: 15px;
}
.btns *{
  margin: 0;padding: 0;
  color: var(--main-orange);
  background: var(--main-blue);
  border-radius: 20px;
}
.btns button:not([disabled]):hover{
  box-shadow: 0 0 10px var(--main-blue);
}
</style>
