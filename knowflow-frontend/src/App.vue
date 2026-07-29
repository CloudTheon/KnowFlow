<template>
  <t-config-provider :global-config="{ classPrefix: 't' }">
    <router-view />
  </t-config-provider>
</template>

<script setup lang="ts">
</script>

<style>
@import './styles/index.css';
</style>
