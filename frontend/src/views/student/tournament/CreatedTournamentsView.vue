<template>
  <div class="container">
    <h2>Created Tournaments</h2>

    <!-- WEB BROWSER -->
    <v-card class="table" v-if="!isMobile">
      <v-data-table
        :headers="headers"
        :items="createdTournaments"
        :search="search"
        multi-sort
        :mobile-breakpoint="0"
        :items-per-page="15"
        :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
        no-data-text="No Created Tournaments"
        no-results-text="No Tournaments Found"
        data-cy="createdTournamentsTable"
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
        <template v-slot:item.status="{ item }">
          <v-chip-group>
            <v-chip data-cy="status">
              {{ getStatus(item) }}
            </v-chip>
          </v-chip-group>
        </template>
        <template v-slot:item.action="{ item }">
          <v-tooltip bottom>
            <template
              v-slot:activator="{ on }"
              v-if="
                getStatus(item) == 'Created' || getStatus(item) == 'Available'
              "
            >
              <v-icon
                medium
                class="mr-2"
                v-on="on"
                @click="cancelTournament(item)"
                data-cy="cancelTournament"
                color="red"
                >fas fa-ban</v-icon
              >
            </template>
            <span>Cancel Tournament</span>
          </v-tooltip>
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                medium
                class="mr-2"
                v-on="on"
                @click="deleteTournament(item)"
                data-cy="deleteTournament"
                color="red"
                >delete</v-icon
              >
            </template>
            <span>Delete Tournament</span>
          </v-tooltip>
        </template>
      </v-data-table>
    </v-card>

    <!-- MOBILE -->
    <v-card class="table" v-else>
      <v-data-table
        :headers="headers_mobile"
        :items="createdTournaments"
        :search="search"
        multi-sort
        :mobile-breakpoint="0"
        :items-per-page="15"
        :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
        no-data-text="No Created Tournaments"
        no-results-text="No Tournaments Found"
        data-cy="createdTournamentsTable"
      >
        <template v-slot:top>
          <v-card-title>
            <v-row>
              <v-col cols="9">
                <v-text-field
                  v-model="search"
                  append-icon="search"
                  label="Search"
                  class="mx-2"
                  data-cy="search-input"
                />
              </v-col>
              <v-col cols="3" align-self="center">
                <template>
                  <v-btn fab primary small color="primary">
                    <v-icon small class="mr-2" @click="newTournament"
                      >fa fa-plus</v-icon
                    >
                  </v-btn>
                </template>
              </v-col>
            </v-row>
            <edit-tournament-dialog
              v-if="currentTournament"
              v-model="editTournamentDialog"
              :tournament="currentTournament"
              :topics="topics"
              @saveTournament="createdTournament"
            />
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
                  <v-col cols="6">
                    <span>Status:</span>
                  </v-col>
                  <v-col>
                    {{ getStatus(item) }}
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="6">
                    <span>Available Date:</span>
                  </v-col>
                  <v-col>
                    <span>{{ item.availableDate }}</span>
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
                  <v-spacer />
                  <v-btn
                    v-if="
                      getStatus(item) == 'Created' ||
                        getStatus(item) == 'Available'
                    "
                    fab
                    color="error"
                    small
                    @click="cancelTournament(item)"
                  >
                    <v-icon medium class="mr-2">
                      fas fa-ban
                    </v-icon>
                  </v-btn>
                  <v-btn
                    fab
                    color="error"
                    small
                    @click="deleteTournament(item)"
                  >
                    <v-icon medium class="mr-2">
                      delete
                    </v-icon>
                  </v-btn>
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
import Tournament from '@/models/management/Tournament';
import RemoteServices from '@/services/RemoteServices';
import Topic from '@/models/management/Topic';
import EditTournamentDialog from './EditTournamentDialog.vue';

@Component({
  components: {
    'edit-tournament-dialog': EditTournamentDialog
  }
})
export default class CreatedTournamentsView extends Vue {
  isMobile: boolean = false;
  createdTournaments: Tournament[] = [];
  currentTournament: Tournament | null = null;
  editTournamentDialog: boolean = false;
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
      width: '20%',
      sortable: false
    },
    {
      text: 'Nº of Questions',
      value: 'numberOfQuestions',
      align: 'center',
      width: '5%'
    },
    {
      text: 'Participants',
      value: 'participants.length',
      align: 'center',
      width: '5%'
    },
    {
      text: 'Creation Date',
      value: 'creationDate',
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
      text: 'Status',
      value: 'status',
      align: 'center',
      width: '10%'
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
    await this.getCreatedTournaments();
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
    await this.getCreatedTournaments();
    await this.$store.dispatch('clearLoading');
  }

  async deleteTournament(tournament: Tournament) {
    try {
      await RemoteServices.deleteTournament(tournament);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('loading');
    await this.getCreatedTournaments();
    await this.$store.dispatch('clearLoading');
  }

  async cancelTournament(tournament: Tournament) {
    try {
      await RemoteServices.cancelTournament(tournament);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('loading');
    await this.getCreatedTournaments();
    await this.$store.dispatch('clearLoading');
  }

  async getCreatedTournaments() {
    try {
      this.createdTournaments = await RemoteServices.getCreatedTournaments();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  getStatus(tournament: Tournament) {
    let date = Date.now();
    if (tournament.isCancelled) return 'Cancelled';
    else if (date < Date.parse(tournament.availableDate)) return 'Created';
    else if (date < Date.parse(tournament.runningDate)) return 'Available';
    else if (date < Date.parse(tournament.conclusionDate)) return 'Running';
    else return 'Finished';
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
