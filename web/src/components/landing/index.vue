<template>
        <div class="dynamic-div">
            <div class="py-3 light">
                <h1>DEVOX</h1>
                <h2>INSPIRING XPER</h2>
                <router-link :to="{ name: 'login'}" class="btn btn-primary">Se Connecter</router-link>
            </div>
        </div>
</template>

<script>
export default {
    name:'landing'
}
</script>

<style scoped>
    h1{
        font-size: clamp(4em,50%, 8em);
        letter-spacing: 2px;
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    }
    h2{
        margin-top: -20px;        
        background: -webkit-linear-gradient(var(--color-4), var(--color-2));
        background-clip: text;
        -webkit-text-fill-color: transparent;
    }
    
</style>