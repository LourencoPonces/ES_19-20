<template>
  <div class="">
    <v-row data-cy="requestsSubmittedRow">
      <v-col><b>Clarification Requests Submitted:</b></v-col>
      <v-col v-if="stats.isPublic(stats.statsNames.REQUESTS_SUBMITTED)">{{
        stats.getStatValue(stats.statsNames.REQUESTS_SUBMITTED)
      }}</v-col>
      <v-col v-else>Private</v-col>
    </v-row>
    <v-divider />
    <v-row data-cy="publicRequestsRow">
      <v-col><b>Public Clarification Requests:</b></v-col>
      <v-col v-if="stats.isPublic(stats.statsNames.PUBLIC_REQUESTS)">{{
        stats.getStatValue(stats.statsNames.PUBLIC_REQUESTS)
      }}</v-col>
      <v-col v-else>Private</v-col>
    </v-row>
    <v-divider />
    <v-row data-cy="submittedQuestionsRow">
      <v-col><b>Submitted Questions</b></v-col>
      <v-col v-if="stats.isPublic(stats.statsNames.SUBMITTED_QUESTIONS)">{{
        stats.getStatValue(stats.statsNames.SUBMITTED_QUESTIONS)
      }}</v-col>
      <v-col v-else>Private</v-col>
    </v-row>
    <v-divider />
    <v-row data-cy="approvedQuestionsRow">
      <v-col><b>Approved Questions</b></v-col>
      <v-col v-if="stats.isPublic(stats.statsNames.APPROVED_QUESTIONS)">{{
        stats.getStatValue(stats.statsNames.APPROVED_QUESTIONS)
      }}</v-col>
      <v-col v-else>Private</v-col>
    </v-row>
    <v-divider />
    <v-row data-cy="tournamentsParticipatedRow">
      <v-col><b>Tournaments Participated:</b></v-col>
      <v-col v-if="stats.isPublic(stats.statsNames.TOURNAMENTS_PARTICIPATED)">{{
        stats.getStatValue(stats.statsNames.TOURNAMENTS_PARTICIPATED)
      }}</v-col>
      <v-col v-else>Private</v-col>
    </v-row>
    <v-divider />
    <v-row data-cy="tournamentsScoreRow">
      <v-col><b>Tournaments Score:</b></v-col>
      <v-col v-if="stats.isPublic(stats.statsNames.TOURNAMENTS_SCORE)">{{
        stats.getStatValue(stats.statsNames.TOURNAMENTS_SCORE)
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
