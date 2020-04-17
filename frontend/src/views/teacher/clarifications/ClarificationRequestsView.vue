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
      <template v-slot:item.hasAnswer="{ item }">
        <v-simple-checkbox
          :value="item.hasAnswer"
          readonly
          :aria-label="item.hasAnswer"
        />
      </template>
      <template v-slot:item.actions="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-btn
              v-on="on"
              text
              @click="openAnswerDialog(item)"
              :data-cy="'answerClarification-' + item.content.slice(0, 15)"
            >
              <v-icon small class="mr-2">forum</v-icon>
            </v-btn>
          </template>
          <span>Manage Answer</span>
        </v-tooltip>
      </template>
    </v-data-table>
    <v-dialog v-model="answerDialog" max-width="75%">
      <v-card>
        <v-card-title>
          <span class="headline">Answer Question</span>
        </v-card-title>

        <v-card-text width="100%">
          <div v-if="answerDialog" class="answer-context">
            <h2>Question:</h2>
            <show-question :question="questionForRequestBeingAnswered" />

            <h2>Clarification Request:</h2>
            <span class="multiline">{{ requestBeingAnswered.content }}</span>
          </div>

          <v-textarea
            v-model="answerInCreation.content"
            label="Answer"
            data-cy="answerField"
          />
        </v-card-text>

        <v-card-actions>
          <v-spacer />
          <v-btn color="secondary" @click="closeDialogue">Cancel</v-btn>
          <v-btn
            id="delete-btn"
            color="red"
            @click="deleteAnswer"
            v-if="!isNewAnswer"
            data-cy="answerDelete"
            >Delete Answer</v-btn
          >

          <v-btn
            color="primary"
            @click="submitAnswer"
            v-if="isNewAnswer"
            data-cy="answerSubmit"
            >Submit Answer</v-btn
          >
          <v-btn
            color="primary"
            @click="submitAnswer"
            v-else
            data-cy="answerSubmit"
            >Update Answer</v-btn
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
import ShowQuestion from '../questions/ShowQuestion.vue';
import Question from '../../../models/management/Question';
import User from '../../../models/user/User';

@Component({
  components: { 'show-question': ShowQuestion }
})
export default class ClarificationRequestsView extends Vue {
  clarifications: ClarificationRequest[] = [];
  expand: boolean = false;
  answerDialog: boolean = false;
  requestBeingAnswered: ClarificationRequest = new ClarificationRequest();
  questionForRequestBeingAnswered: Question = new Question();
  answerInCreation: ClarificationRequestAnswer = new ClarificationRequestAnswer();
  isNewAnswer: boolean = false;
  questionCache: Record<number, Question> = {};
  userCache: Record<number, User> = {};
  search: string = '';
  headers: object = [
    { text: 'Clarification Request', value: 'content', align: 'left' },
    {
      text: 'Answered',
      value: 'hasAnswer',
      width: '33px'
    },
    {
      text: 'Creation Date',
      value: 'creationDate',
      align: 'center',
      width: '150px'
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
      this.clarifications = await RemoteServices.getClarificationRequests();
    } catch (error) {
      await this.$store.dispatch('error', error);
    } finally {
      await this.$store.dispatch('clearLoading');
    }
  }

  customFilter(value: string, search: string): boolean {
    // noinspection SuspiciousTypeOfGuard,SuspiciousTypeOfGuard
    return (
      search != null &&
      typeof value === 'string' &&
      value.toLocaleLowerCase().indexOf(search.toLocaleLowerCase()) !== -1
    );
  }

  async openAnswerDialog(req: ClarificationRequest): Promise<void> {
    try {
      this.questionForRequestBeingAnswered = await RemoteServices.getQuestionById(
        req.getQuestionId()
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
      return;
    }

    if (req.hasAnswer) {
      this.answerInCreation = req.getAnswer();
      this.isNewAnswer = false;
    } else {
      this.answerInCreation = req.newAnswer();
      this.isNewAnswer = true;
    }
    this.requestBeingAnswered = req;
    this.answerDialog = true;
  }

  closeDialogue(): void {
    this.answerDialog = false;
  }

  async submitAnswer(): Promise<void> {
    const answerInCreation = this
      .answerInCreation as ClarificationRequestAnswer;

    await this.$store.dispatch('loading');
    try {
      // publish answer
      const ans = await RemoteServices.submitClarificationRequestAnswer(
        answerInCreation
      );

      // save change locally
      this.requestBeingAnswered.setAnswer(ans);

      this.closeDialogue();
    } catch (err) {
      await this.$store.dispatch('error', err);
    } finally {
      await this.$store.dispatch('clearLoading');
    }
  }

  async deleteAnswer(): Promise<void> {
    const answerInCreation = this
      .answerInCreation as ClarificationRequestAnswer;

    await this.$store.dispatch('loading');
    try {
      // publish answer
      await RemoteServices.deleteClarificationRequestAnswer(answerInCreation);

      // save change locally
      this.requestBeingAnswered.setAnswer(null);

      this.closeDialogue();
    } catch (err) {
      await this.$store.dispatch('error', err);
    } finally {
      await this.$store.dispatch('clearLoading');
    }
  }
}
</script>

<style lang="scss" scoped>
.answer-context {
  text-align: left;
  margin-bottom: 20px;

  h2 {
    margin-top: 10px;
    margin-bottom: 10px;
  }
}

#delete-btn {
  color: #ffffff;
}
</style>
