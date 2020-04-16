<template>
  <v-card class="table">
    <h1>Available Tournaments</h1>

    <v-data-table
            :headers="headers"
            :custom-filter="customFilter"
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
             <span>{{item.creator.username}}</span>
      </template>
    </v-data-table>

  </v-card>
</template>

<script lang="ts">
  import { Component, Vue} from 'vue-property-decorator';
  import Tournament from '@/models/management/Tournament';
  import RemoteServices from '@/services/RemoteServices';

  @Component({
  })

  export default class AvailableTournamentsView extends Vue {
    availableTournaments: Tournament[] = [];
    search: string = '';

    headers: object = [
      { text: 'Title',
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
    ];

    async created() {
      await this.$store.dispatch('loading');
      try {
        this.availableTournaments = await RemoteServices.getAvailableTournaments();
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
      await this.$store.dispatch('clearLoading');
    }

    customFilter(
    ) {

    }

  }
</script>

<style lang="scss"></style>
