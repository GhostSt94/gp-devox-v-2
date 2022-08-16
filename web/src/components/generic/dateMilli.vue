<template>
    <input type="date" :class="{'show-date-placeholder': showPlaceholder}" :disabled="disabled" class="form-control" v-on:input="$emit('input', convertToMillis($event.target.value))"
           :value="convertToDate(value)" :max="maxDate">
</template>

<script>
    export default {
        name: "dateMilli",
        props:{
            value:{
                type: Number
            },
            showPlaceholder:{
                type: Boolean,
                default: false
            },
            maxDate: {
                type: String,
                default: null
            },
            disabled: {
                type: Boolean,
                default: false
            }
        },
        data() {
            return {
                type: 'date'
            }
        },
        methods:{
            convertToMillis(dateOutput){
                return dateOutput !=null ? this.$moment(dateOutput).valueOf() : null;
            },
            convertToDate(dateInput){
                return dateInput ? this.$moment(dateInput).format("YYYY-MM-DD") : null;
            }
        }

    }
</script>
<style>
    .show-date-placeholder::before{
        color: #999;
        content: attr(placeholder) " :";
    }
    .show-date-placeholder:focus::before {
        content: "" !important;
    }
</style>
