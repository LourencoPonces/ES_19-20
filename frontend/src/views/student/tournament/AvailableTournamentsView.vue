<template>
  <div class="container">
    <h2>Available Tournaments</h2>
    <v-card class="table" v-if="!isMobile">
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
        data-cy="available-tournaments"
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
          </v-card-title>
        </template>
        <template v-slot:item.topics="{ item }">
          <v-chip-group data-cy="topics-list">
            <v-chip v-for="topic in item.topics" :key="topic.name">
              {{ topic.name }}
            </v-chip>
          </v-chip-group>
        </template>
        <template v-slot:item.creator="{ item }">
          <span>{{ item.creator.username }}</span>
        </template>
        <template v-slot:item.sign-up-status="{ item }" data-cy="status">
          <v-simple-checkbox
            :value="signedUpTournaments.includes(item)"
            readonly
            color="green"
            :aria-label="signedUpTournaments.includes(item)"
          />
        </template>
        <template v-slot:item.action="{ item }">
          <v-tooltip bottom>
            <template
              v-slot:activator="{ on }"
              v-if="!signedUpTournaments.includes(item)"
            >
              <v-icon
                medium
                class="mr-2"
                v-on="on"
                @click="signUpInTournament(item)"
                >fas fa-sign-in-alt</v-icon
              >
            </template>
            <span>Sign Up</span>
          </v-tooltip>
        </template>
      </v-data-table>
    </v-card>
    <v-card class="table" v-else>
      <v-data-table
        :headers="headers_mobile"
        :items="availableTournaments"
        :search="search"
        multi-sort
        :mobile-breakpoint="0"
        :items-per-page="15"
        :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
        no-data-text="No Available Tournaments"
        no-results-text="No Tournaments Found"
        data-cy="available-tournaments"
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
          </v-card-title>
        </template>
        <template v-slot:item.title="{ item }">
          <v-expansion-panels flat>
            <v-expansion-panel>
              <v-expansion-panel-header>
                <p>{{ item.title }}</p>
              </v-expansion-panel-header>
              <v-expansion-panel-content>
                <v-row>
                  <v-col>
                    <span>Created by {{ item.creator.username }}</span>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="6">
                    <span>Running Date:</span>
                  </v-col>
                  <v-col>
                    <span>{{ item.runningDate }}</span>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="6">
                    <span>Conclusion Date:</span>
                  </v-col>
                  <v-col>
                    <span>{{ item.conclusionDate }}</span>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col>
                    <span>{{ item.numberOfQuestions }} questions</span>
                  </v-col>
                  <v-col>
                    <span
                      >{{ item.participants.length }} participant{{
                        item.participants.length === 1 ? '' : 's'
                      }}</span
                    >
                  </v-col>
                </v-row>
                <v-row>
                  <v-col>
                    <span><b>Topics</b></span>
                    <br />
                    <v-chip-group data-cy="topics-list" column>
                      <v-chip v-for="topic in item.topics" :key="topic.name">
                        {{ topic.name }}
                      </v-chip>
                    </v-chip-group>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col>
                    <v-checkbox
                      v-model="selected"
                      label="Signed In"
                      :value="item.id"
                      :disabled="selected.includes(item.id)"
                      @change="signUpInTournament(item)"
                    ></v-checkbox>
                  </v-col>
                </v-row>
              </v-expansion-panel-content>
            </v-expansion-panel>
          </v-expansion-panels>
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
  isMobile: boolean = false;
  selected = [];
  availableTournaments: Tournament[] = [];
  signedUpTournaments: Tournament[] = [];
  topics!: Topic[];
  search: string = '';

  headers: object = [
    {
      text: 'Actions',
      value: 'action',
      align: 'center',
      width: '10%',
      sortable: false
    },
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
      text: 'Participants',
      value: 'participants.length',
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
      text: 'Signed Up',
      value: 'sign-up-status',
      align: 'center',
      width: '10%',
      sortable: false
    }
  ];

  headers_mobile = [
    {
      text: 'Tournament',
      value: 'title',
      align: 'center',
      sortable: false
    }
  ];

  async created() {
    await this.$store.dispatch('loading');
    this.isMobile = window.innerWidth <= 500;
    try {
      this.topics = await RemoteServices.getTopics();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.getAvailableTournaments();
    await this.$store.dispatch('clearLoading');
  }

  async signUpInTournament(tournament: Tournament, event) {
    if (this.isMobile) {
      console.log(tournament);
    }
    await this.$store.dispatch('loading');
    if (tournament.id)
      try {
        await RemoteServices.signUpInTournament(tournament.id);
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

  getSignUpTournaments() {
    if (this.availableTournaments)
      for (let tournament of this.availableTournaments)
        for (let participant of tournament.participants)
          if (Store.getters.getUser.username == participant.username) {
            this.signedUpTournaments.push(tournament);
            this.selected.push(tournament.id);
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
