<template>
  <div class="container">
    <h2>Import/Export</h2>
    <v-btn color="primary" @click="exportAll">Export ZIP</v-btn>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';

@Component
export default class ImpExptView extends Vue {
  async exportAll() {
    try {
      await RemoteServices.exportAll();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  max-width: 90%;
  margin-left: auto;
  margin-right: auto;
  padding-left: 10px;
  padding-right: 10px;

  h2 {
    font-size: 26px;
    margin: 20px 0;
    text-align: center;
    small {
      font-size: 0.5em;
    }
  }
}
</style>
