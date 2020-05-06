<template>
  <div class="container">
    <h2>Created Tournaments</h2>
    <v-card class="table">
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
            <v-chip>
              {{ getStatus(item) }}
            </v-chip>
          </v-chip-group>
        </template>





        <template v-slot:item.action="{ item }">


                    <v-tooltip bottom>
                      <template v-slot:activator="{ on }">
                        <v-icon
                                large
                                class="mr-2"
                                v-on="on"
                                @click="cancelTournament(item)"
                                color="red"
                        >fas fa-ban</v-icon
                        >
                      </template>
                      <span>Cancel Tournament</span>
                    </v-tooltip>


                    <v-tooltip bottom><!--bottom v-if="item.isChangeable()"-->
                      <template v-slot:activator="{ on }">
                        <v-icon
                                large
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
            createdTournaments: Tournament[] = [];
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
              },
              {
                text: 'Actions',
                value: 'action',
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
              console.log('--------------------------');
              console.log(tournament.isCancelled);
              console.log(tournament.title);
              console.log('--------------------------');
              if (tournament.isCancelled)
                return 'Cancelled';
              else
                return 'Available';
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
