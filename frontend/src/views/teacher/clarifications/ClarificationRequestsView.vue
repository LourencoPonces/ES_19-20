<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :custom-filter="customFilter"
      :items="clarifications"
      :search="search"
      :mobile-breakpoint="0"
      :items-per-page="50"
      show-expand
      single-expand
      class="table"
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
        <div
          class="short-content"
          :data-cy="'req-' + item.content.slice(0, 15)"
        >
          {{ item.content }}
        </div>
      </template>

      <template v-slot:item.resolved="{ item }">
        <v-simple-checkbox
          :value="item.resolved"
          readonly
          :aria-label="
            item.resolved ? 'request was resolved' : 'request was not resolved'
          "
        />
      </template>

      <template v-slot:item.status="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-btn
              v-on="on"
              text
              @click="changeRequestStatus(item)"
              :data-cy="'changeStatus-' + item.content.slice(0, 15)"
              :aria-label="item.isPrivate() ? 'Make Public' : 'Make Private'"
            >
              <v-icon
                v-if="item.isPrivate()"
                small
                class="mr-2"
                :data-cy="'private-' + item.content.slice(0, 15)"
                >fas fa-eye-slash</v-icon
              >
              <v-icon
                v-else
                small
                class="mr-2"
                :data-cy="'public-' + item.content.slice(0, 15)"
                >fas fa-eye</v-icon
              >
            </v-btn>
          </template>
          <span v-if="item.isPrivate()">Make Public</span>
          <span v-else>Make Private</span>
        </v-tooltip>
      </template>

      <template v-slot:expanded-item="{ headers, item }">
        <td :colspan="headers.length" class="clarification-expand-container">
          <h2>Question:</h2>
          <show-question :question="questionCache[item.questionId]" />

          <h2>Clarification Request:</h2>
          <div class="multiline msg-content">{{ item.content }}</div>

          <h3>Messages:</h3>
          <clarification-thread :request="item"></clarification-thread>
        </td>
      </template>
    </v-data-table>
  </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import ClarificationRequest from '@/models/clarification/ClarificationRequest';
import ClarificationMessage from '@/models/clarification/ClarificationMessage';
import ShowQuestion from '../questions/ShowQuestion.vue';
import Question from '../../../models/management/Question';
import User from '../../../models/user/User';
import ClarificationThread from '../../ClarificationThread.vue';

@Component({
  components: {
    'show-question': ShowQuestion,
    'clarification-thread': ClarificationThread
  }
})
export default class ClarificationRequestsView extends Vue {
  clarifications: ClarificationRequest[] = [];
  questionCache: Record<number, Question> = {};
  search: string = '';
  headers: object = [
    {
      text: 'Visibility',
      value: 'status',
      align: 'center',
      width: '100px',
      sortable: false
    },
    {
      text: 'Clarification Request',
      value: 'content',
      align: 'left'
    },
    {
      text: 'Resolved',
      value: 'resolved',
      width: '110px'
    },
    {
      text: 'Creation Date',
      value: 'creationDate',
      align: 'center',
      width: '150px'
    }
  ];

  async created(): Promise<void> {
    await this.$store.dispatch('loading');
    try {
      this.clarifications = await RemoteServices.getUserClarificationRequests();

      this.questionCache = await this.loadQuestions(this.clarifications);
    } catch (error) {
      await this.$store.dispatch('error', error);
    } finally {
      await this.$store.dispatch('clearLoading');
    }
  }

  private async loadQuestions(
    requests: ClarificationRequest[]
  ): Promise<Record<number, Question>> {
    // TODO: include question in clarificationrequest, avoid this ugly, potientially slow, code
    const questions: Record<number, Question> = {};

    for (const req of requests) {
      if (!questions[req.questionId]) {
        questions[req.questionId] = await RemoteServices.getQuestionById(
          req.questionId
        );
      }
    }

    return questions;
  }

  customFilter(value: string, search: string): boolean {
    // noinspection SuspiciousTypeOfGuard,SuspiciousTypeOfGuard
    return (
      search != null &&
      typeof value === 'string' &&
      value.toLocaleLowerCase().indexOf(search.toLocaleLowerCase()) !== -1
    );
  }

  async changeRequestStatus(req: ClarificationRequest): Promise<void> {
    await this.$store.dispatch('loading');
    try {
      const status = req.isPrivate() ? 'PUBLIC' : 'PRIVATE';
      let result = await RemoteServices.changeClarificationRequestStatus(
        req.getId(),
        status
      );

      this.clarifications.splice(this.clarifications.indexOf(req), 1, result);
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

.clarification-expand-container {
  text-align: left;
  padding: 20px;

  h2,
  h3 {
    margin: 10px 0;
    font-size: initial;
    text-align: left;
  }

  h2 {
    font-size: 20px;
  }

  .msg-content {
    margin-bottom: 10px;
  }
}

#delete-btn {
  color: #ffffff;
}

.multiline {
  white-space: pre-wrap;
}

.short-content {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

::v-deep .table table {
  table-layout: fixed;
  min-width: 600px;
}
</style>
