<template>
  <v-card class="table">
    <h1>Available Tournaments</h1>

    <v-data-table
      :headers="headers"
      :items="availableTournaments"
      :search="search"
      multi-sort
      :mobile-breakpoint="0"
      :items-per-page="15"
      :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
      no-data-text="No Available Tournaments"
      no-results-text="No Tournaments Found"
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
        </v-card-title>
      </template>

      <template v-slot:item.topics="{ item }">
        <v-chip-group>
          <v-chip v-for="topic in item.topics" :key="topic.name">
            {{ topic.name }}
          </v-chip>
        </v-chip-group>
      </template>

      <template v-slot:item.creator="{ item }">
        <span>{{ item.creator.username }}</span>
      </template>


      <template v-slot:item.delete-button="{ item }">
        <v-btn color="red" @click="deleteTournament(item)">Delete</v-btn>
      </template>
    </v-data-table>
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import Store from '@/store';
import Tournament from '@/models/management/Tournament';
import RemoteServices from '@/services/RemoteServices';
import Topic from '@/models/management/Topic';
import EditTournamentDialog from './EditTournamentDialog.vue';

@Component({
  components: {
    'edit-tournament-dialog': EditTournamentDialog
  }
})
export default class AvailableTournamentsView extends Vue {
  availableTournaments: Tournament[] = [];
  signedUpTournaments: Tournament[] = [];
  currentTournament: Tournament | null = null;
  editTournamentDialog: boolean = false;
  topics!: Topic[];
  search: string = '';

  headers: object = [
    {
      text: 'Title',
      value: 'title',
      align: 'center',
      width: '20%'
    },
    {
      text: 'Topics',
      value: 'topics',
      align: 'center',
      width: '10%',
      sortable: false
    },
    {
      text: 'Nº of Questions',
      value: 'numberOfQuestions',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Available Date',
      value: 'availableDate',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Running Date',
      value: 'runningDate',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Conclusion Date',
      value: 'conclusionDate',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Creator',
      value: 'creator',
      align: 'center',
      width: '10%',
      sortable: false
    },
    {
      value: 'delete-button',
      align: 'center',
      width: '10%',
      sortable: false
    }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.topics = await RemoteServices.getTopics();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.getAvailableTournaments();
    await this.$store.dispatch('clearLoading');
  }

  async getAvailableTournaments() {
    try {
      this.availableTournaments = await RemoteServices.getAvailableTournaments();
      this.getSignUpTournaments();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

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

  async createdTournament(tournament: Tournament) {
    this.editTournamentDialog = false;
    await this.getAvailableTournaments();
  }
  
  getSignUpTournaments() {
    for (let i = 0; i < this.availableTournaments.length; i++)
      for (let j = 0; j < this.availableTournaments[i].participants.length; j++)
        if (
          Store.getters.getUser.username ==
          this.availableTournaments[i].participants[j].username
        ) {
          this.signedUpTournaments.push(this.availableTournaments[i]);
          break;
        }
  }

  async deleteTournament(tournament: Tournament) {
    try {
      await RemoteServices.deleteTournament(tournament);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }

    await this.$store.dispatch('loading');
    try {
      this.availableTournaments = await RemoteServices.getAvailableTournaments();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
}
</script>

<style lang="scss"></style>
