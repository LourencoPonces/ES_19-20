<template>
  <div v-if="userStats">
    <v-dialog
      :value="dialog"
      @input="$emit('dialog', false)"
      @keydown.esc="$emit('dialog', false)"
      max-width="75%"
    >
      <v-card>
        <v-card-title>
          <span class="headline">{{ username }}</span>
        </v-card-title>

        <v-card-text class="text-left">
          <show-dashboard-stats :stats="userStats" />
        </v-card-text>

        <v-card-actions>
          <v-spacer />
          <v-btn dark color="blue darken-1" @click="$emit('dialog')"
            >close</v-btn
          >
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model } from 'vue-property-decorator';
import DashboardStats from '@/models/statement/DashboardStats';
import RemoteServices from '@/services/RemoteServices';
import ShowDashboardStats from '@/views/student/dashboard/ShowDashboardStats.vue';

@Component({
  components: {
    'show-dashboard-stats': ShowDashboardStats
  }
})
export default class ShowDashboardStatsDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ required: true })
  readonly username!: string;
  userStats: DashboardStats | null = null;

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.userStats = await RemoteServices.getUserDashboardStats(
        this.username
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
}
</script>
