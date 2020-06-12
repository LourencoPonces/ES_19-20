<template>
  <div class="container">
    <h2>Solved Tournaments</h2>
    <v-card class="table">
      <v-data-table
        :headers="headers"
        :items="solvedTournaments"
        :search="search"
        multi-sort
        :mobile-breakpoint="0"
        :items-per-page="15"
        :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
        no-data-text="No Solved Tournaments"
        no-results-text="No Tournaments Found"
        data-cy="solvedTournamentsTable"
      >
        <template v-slot:top>
          <v-card-title>
            <v-text-field
              v-model="search"
              append-icon="search"
              label="Search"
              class="mx-2"
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
      </v-data-table>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
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
  solvedTournaments: Tournament[] = [];
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
      width: '20%',
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
      width: '20%'
    },
    {
      text: 'Conclusion Date',
      value: 'conclusionDate',
      align: 'center',
      width: '20%'
    }
  ];

  async solved() {
    await this.$store.dispatch('loading');
    try {
      this.topics = await RemoteServices.getTopics();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.getSolvedTournaments();
    await this.$store.dispatch('clearLoading');
  }

  async getSolvedTournaments() {
    try {
      this.solvedTournaments = await RemoteServices.getSolvedTournaments();
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
