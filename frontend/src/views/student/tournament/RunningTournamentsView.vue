<template>
  <div class="container">
    <h2>Running Tournaments</h2>
    <v-card class="table">
      <v-data-table
        :headers="headers"
        :items="runningTournaments"
        :search="search"
        multi-sort
        :mobile-breakpoint="0"
        :items-per-page="15"
        :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
        no-data-text="No Running Tournaments"
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

        <template v-slot:item.solve-quiz-button="{ item }">
          <v-btn
            color="primary"
            data-cy="solveTournament"
            @click="solveTournamentQuiz(item)"
            >Solve</v-btn
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
import StatementManager from '@/models/statement/StatementManager';

@Component
export default class RunningTournamentsView extends Vue {
  runningTournaments: Tournament[] = [];
  search: string = '';

  headers: object = [
    {
      value: 'solve-quiz-button',
      align: 'center',
      width: '5%',
      sortable: false
    },
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
      width: '15%'
    },
    {
      text: 'Running Date',
      value: 'runningDate',
      align: 'center',
      width: '15%'
    },
    {
      text: 'Conclusion Date',
      value: 'conclusionDate',
      align: 'center',
      width: '15%'
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
      width: '15%'
    }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.runningTournaments = await RemoteServices.getSignedUpRunningTournaments();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async solveTournamentQuiz(tournament: Tournament) {
    try {
      let quiz = await RemoteServices.getTournamentQuiz(tournament);
      let statementManager: StatementManager = StatementManager.getInstance;
      statementManager.statementQuiz = quiz;
      alert(JSON.stringify(quiz));
      await this.$router.push({ name: 'solve-quiz' });
    } catch (error) {
      await this.$store.dispatch('error', error);
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
