<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :custom-filter="customFilter"
      :items="clarifications"
      :search="search"
      :mobile-breakpoint="0"
      :items-per-page="50"
      :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
    >
      <template v-slot:top>
        <v-card-title>
          <v-text-field
            v-model="search"
            append-icon="search"
            label="Search"
            single-line
            hide-details
          />
        </v-card-title>
      </template>
      <template v-slot:item.content="{ item }">
        <span style="white-space: pre;">{{ item.content }}</span>
      </template>
      <template v-slot:item.actions="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-btn v-on="on" text @click="openAnswerDialog(item)">
              <v-icon small class="mr-2">forum</v-icon>
            </v-btn>
          </template>
          <span>Answer Request</span>
        </v-tooltip>
      </template>
    </v-data-table>
    <v-dialog v-model="answerDialog" max-width="75%">
      <v-card>
        <v-card-title>
          <span class="headline">Answer Question</span>
        </v-card-title>

        <v-card-text v-if="editedTopic">
          <v-container grid-list-md fluid>
            <v-layout column wrap>
              <v-flex xs24 sm12 md8>
                <v-text-field
                  v-model="answerInCreation.content"
                  label="Answer"
                />
              </v-flex>
            </v-layout>
          </v-container>
        </v-card-text>

        <v-card-actions>
          <v-spacer />
          <v-btn color="blue darken-1" @click="closeDialogue">Cancel</v-btn>
          <v-btn color="blue darken-1" @click="submitAnswer"
            >Submit Answer</v-btn
          >
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import ClarificationRequest from '@/models/clarification/ClarificationRequest';
import ClarificationRequestAnswer from '@/models/clarification/ClarificationRequestAnswer';
import Question from '../../../models/management/Question';
import User from '../../../models/user/User';

@Component
export default class UnansweredClarificationsView extends Vue {
  clarifications: ClarificationRequest[] = [];
  expand: boolean = false;
  answerDialog: boolean = false;
  requestBeingAnswered: ClarificationRequest | undefined = undefined;
  answerInCreation: ClarificationRequestAnswer | undefined = undefined;
  questionCache: Record<number, Question> = {};
  userCache: Record<number, User> = {};
  search: string = '';
  headers: object = [
    { text: 'Clarification Request', value: 'content', align: 'left' },
    {
      text: 'Creation Date',
      value: 'creationDate',
      align: 'center'
    },
    {
      text: 'Actions',
      value: 'actions',
      align: 'center',
      width: '33px',
      sortable: false
    }
  ];

  async created(): Promise<void> {
    await this.$store.dispatch('loading');
    try {
      this.clarifications = await RemoteServices.getUnansweredClarificationRequests();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  customFilter(value: string, search: string): boolean {
    // noinspection SuspiciousTypeOfGuard,SuspiciousTypeOfGuard
    return (
      search != null &&
      typeof value === 'string' &&
      value.toLocaleLowerCase().indexOf(search.toLocaleLowerCase()) !== -1
    );
  }

  openAnswerDialog(req: ClarificationRequest): void {
    this.answerInCreation = req.newAnswer();
    this.requestBeingAnswered = req;
    this.answerDialog = true;
  }

  closeDialogue(): void {
    this.answerDialog = false;
    this.answerInCreation = undefined;
    this.requestBeingAnswered = undefined;
  }

  async submitAnswer(): Promise<void> {}
}
</script>

<style scoped></style>
