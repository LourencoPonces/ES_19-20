<template>
  <v-card class="table">

    <v-data-table
            :headers="headers"
            :items="availableTournaments"
            :search="search"
            multi-sort
            :mobile-breakpoint="0"
            :items-per-page="15"
            :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
            no-data-text = "No Available Tournaments"
            no-results-text = "No Tournaments Found"
    >

      <template v-slot:top>
        <v-card-title>
          <v-text-field
                  v-model="search"
                  append-icon="search"
                  label="Search"
                  class="mx-2"
          />

          <v-spacer />
          <v-btn color="primary" dark @click="$emit('')">New Tournament</v-btn>
        </v-card-title>
      </template>

    </v-data-table>

  <!-- Remove -->
    <v-card class="table">
      <v-btn color="primary" dark @click="newTournament">New Tournament</v-btn>
      <edit-tournament-dialog
        v-if="currentTournament"
        v-model="editTournamentDialog"
        :tournament="currentTournament"
        :topics="topics"
        @saveTournament="createdTournament"
      />
    </v-card>
  </v-card>
</template>

<script lang="ts">
  import { Component, Vue, Prop } from 'vue-property-decorator';
  import Tournament from "@/models/management/Tournament";
  import RemoteServices from '@/services/RemoteServices';
  import Topic from '@/models/management/Topic';
  import EditTournamentDialog from '@/views/student/tournament/EditTournamentDialog.vue';

  @Component({
    components: {
      'edit-tournament-dialog': EditTournamentDialog
    }
  })

  export default class AvailableTournamentsView extends Vue {
    availableTournaments: Tournament[];
    search: string = '';
    headers: object = [
      { text: 'Title',
        value: 'title',
        align: 'center',
        width: '20%'
      },
      {
        text: 'Nº of Questions',
        value: '',
        align: 'center',
        width: '10%'
      },
      {
        text: 'Topic',
        value: '',
        align: 'center',
        width: '20%'
      },
      {
        text: 'Available Date',
        value: '',
        align: 'center',
        width: '10%'
      },
      {
        text: 'Conclusion Date',
        value: '',
        align: 'center',
        width: '10%'
      },
      {
        text: 'Status',
        value: '',
        align: 'center',
        width: '10%'
      },
      {
        text: 'Creator',
        value: '',
        align: 'center',
        width: '10%'
      },
      {
        text: 'Actions',
        value: '',
        align: 'center',
        width: '10%'
      },
    ];

  }

export default class TournamentsView extends Vue {
  currentTournament: Tournament | null = null;
  editTournamentDialog: boolean = false;
  topics!: Topic[];

  newTournament() {
    this.currentTournament = new Tournament();
    this.editTournamentDialog = true;
  }

  //@Watch('editTournamentDialog')
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

<style lang="scss"></style>
