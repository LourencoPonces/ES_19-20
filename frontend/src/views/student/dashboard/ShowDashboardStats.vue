<template>
  <div class="">
    <v-row>
      <v-col><b>Clarification Requests Submitted:</b></v-col>
      <v-col v-if="stats.isPublic('requestsSubmittedVisibility')">{{
        stats.requestsSubmittedStat
      }}</v-col>
      <v-col v-else>Private</v-col>
    </v-row>
    <v-divider />
    <v-row>
      <v-col><b>Public Clarification Requests:</b></v-col>
      <v-col v-if="stats.isPublic('publicRequestsVisibility')">{{
        stats.publicRequestsStat
      }}</v-col>
      <v-col v-else>Private</v-col>
    </v-row>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import DashboardStats from '@/models/statement/DashboardStats';

@Component
export default class ShowDashboardStats extends Vue {
  @Prop({ type: DashboardStats, required: true })
  readonly stats!: DashboardStats;

  convertMarkDown(val: number): string {
    return convertMarkDown(val.toString(), null);
  }
}
</script>
