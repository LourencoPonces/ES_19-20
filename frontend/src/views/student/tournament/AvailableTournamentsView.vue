<template>
  <div class="container">
    <h2>Available Tournaments</h2>
    <v-card class="table">
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
          <v-chip-group data-cy="topics-list">
            <v-chip v-for="topic in item.topics" :key="topic.name">
              {{ topic.name }}
            </v-chip>
          </v-chip-group>
        </template>

        <template v-slot:item.sign-up-button="{ item }">
          <v-btn color="primary" @click="signUpInTournament(item)">Sign-up</v-btn>
        </template>

        <template v-slot:item.creator="{ item }">
          <span>{{ item.creator.username }}</span>
        </template>

        <template v-slot:item.sign-up-status="{ item }" data-cy="status">
          <div v-if="signedUpTournaments.includes(item)">
            <v-chip color="green" dark>{{ 'Signed-Up' }}</v-chip>
          </div>
          <div v-else>
            <v-btn color="primary" @click="signUpInTournament(item)"
              >Sign-up</v-btn
            >
          </div>
        </template>

        <template v-slot:item.delete-button="{ item }">
          <v-btn
            color="red"
            @click="deleteTournament(item)"
            data-cy="deleteTournament"
            >Delete</v-btn
          >
        </template>
      </v-data-table>
    </v-card>
  </div>
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
      width: '10%'
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
      text: 'Participants',
      value: 'participants.length',
      align: 'center',
      width: '10%'
    },
    {
      value: 'sign-up-status',
      align: 'center',
      width: '10%',
      sortable: false
    },
    {
      value: 'delete-button',
      align: 'center',
      width: '5%',
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

  async createdTournament() {
    this.editTournamentDialog = false;
    await this.$store.dispatch('loading');
    await this.getAvailableTournaments();
    await this.$store.dispatch('clearLoading');
  }

  async signUpInTournament(tournament: Tournament) {
    if (tournament.id)
      try {
        await RemoteServices.signUpInTournament(tournament.id);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }

    await this.$store.dispatch('loading');
    await this.getAvailableTournaments();
    await this.$store.dispatch('clearLoading');
  }

  async deleteTournament(tournament: Tournament) {
    try {
      await RemoteServices.deleteTournament(tournament);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }

    await this.$store.dispatch('loading');
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

  getSignUpTournaments() {
    if (this.availableTournaments)
      for (let tournament of this.availableTournaments)
        for (let participant of tournament.participants)
          if (Store.getters.getUser.username == participant.username) {
            this.signedUpTournaments.push(tournament);
            break;
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
