<template>
   <v-card class="table">
    <v-data-table
      :headers="headers"
      :items="requests"
      :search="search"
      multi-sort
      :mobile-breakpoint="0"
      :items-per-page="15"
      :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
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

      <template v-slot:item.answer="{ item }">
        {{showAnswer(item)}}    
      </template>

      <template v-slot:item.action="{ item }">
        <v-tooltip bottom v-if="!item.hasAnswer()">
          <template v-slot:activator="{ on }">
            <v-icon small class="mr-2" v-on="on" @click="editRequest(item)"
              >edit</v-icon
            >
          </template>
          <span>Edit Request</span>
        </v-tooltip>
        <v-tooltip bottom v-if="!item.hasAnswer()">
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="deleteRequest(item)"
              color="red"
              >delete</v-icon
            >
          </template>
          <span>Delete Request</span>
        </v-tooltip>
      </template>     
    </v-data-table>
  </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import ClarificationRequest from '../../models/clarification/ClarificationRequest';

@Component
export default class ClarificationsView extends Vue {

  requests: ClarificationRequest[] = [];
  search: string = '';

  headers: object = [
    { text: 'Request', value: 'content', align: 'center', sortable: false},
    { text: 'Answer', value: 'answer', align: 'center', sortable: false },
    { text: 'Submission Date', value: 'creationDate', align: 'center'},
    { text: 'Actions', value: 'action', align: 'center', sortable: false }

  ];

    async created() {
      await this.$store.dispatch('loading');
      try {
        this.requests = await RemoteServices.getStudentClarificationRequests();
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
      await this.$store.dispatch('clearLoading');
    }

    showAnswer(request : ClarificationRequest) : string {
        return request.getAnswerContent();
    }

    async deleteRequest(req : ClarificationRequest) {
      if (!confirm('Are you sure you want to delete this clarification request?')) {
        return ;
      }
      await this.$store.dispatch('loading');
      try {
        await RemoteServices.deleteClarificationRequest(req.id);
        this.requests.splice(this.requests.indexOf(req), 1);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
      await this.$store.dispatch('clearLoading');

    
    }
}
</script>

<style scoped></style>
