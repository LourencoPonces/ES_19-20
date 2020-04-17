<template>
  <v-card class="table">
    <v-btn
      color="primary"
      dark
      @click="newTournament"
      data-cy="newTournament"
      >New Tournament</v-btn
    >
    <edit-tournament-dialog
      v-if="currentTournament"
      v-model="editTournamentDialog"
      :tournament="currentTournament"
      :topics="topics"
      @saveTournament="createdTournament"
    />
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Tournament from '@/models/management/Tournament';
import Topic from '@/models/management/Topic';
import EditTournamentDialog from '@/views/student/tournament/EditTournamentDialog.vue';

@Component({
  components: {
    'edit-tournament-dialog': EditTournamentDialog
  }
})
export default class TournamentsView extends Vue {
  currentTournament: Tournament | null = null;
  editTournamentDialog: boolean = false;
  topics!: Topic[];

  newTournament() {
    this.currentTournament = new Tournament();
    this.editTournamentDialog = true;
  }

  @Watch('editTournamentDialog')
  closeError() {
    if (!this.editTournamentDialog) {
      this.currentTournament = null;
    }
  }

  createdTournament(tournament: Tournament) {
    this.editTournamentDialog = false;
  }

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.topics = await RemoteServices.getTopics();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
}
</script>

<style lang="scss" scoped></style>
