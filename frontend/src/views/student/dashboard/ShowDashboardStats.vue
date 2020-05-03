<template>
  <div>
    <ul>
      <li v-if="stats.isPublic(stats.testStatVisibility)">
        <span><b>Test Stat:</b> {{ stats.testStat }}</span>
      </li>
    </ul>
    <br />
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
