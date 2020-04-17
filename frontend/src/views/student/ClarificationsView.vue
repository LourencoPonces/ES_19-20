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
      data-cy="table"
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
        {{ showAnswer(item) }}
      </template>

      <template v-slot:item.action="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="showQuestionDialog(item)"
              >visibility</v-icon
            >
          </template>
          <span>Show Question</span>
        </v-tooltip>
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              v-if="!item.hasAnswer()"
              small
              class="mr-2"
              v-on="on"
              @click="startEditRequest(item)"
              data-cy="edit"
              >edit</v-icon
            >
            <v-icon
              v-else
              disabled
              small
              class="mr-2"
              v-on="on"
              @click="startEditRequest(item)"
              data-cy="edit"
              >edit</v-icon
            >
          </template>
          <span>Edit Request</span>
        </v-tooltip>
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              v-if="!item.hasAnswer()"
              small
              class="mr-2"
              v-on="on"
              @click="deleteRequest(item)"
              color="red"
              data-cy="delete"
              >delete</v-icon
            >
            <v-icon
              v-else
              disabled
              small
              class="mr-2"
              v-on="on"
              @click="deleteRequest(item)"
              color="red"
              data-cy="delete"
              >delete</v-icon
            >
          </template>
          <span>Delete Request</span>
        </v-tooltip>
      </template>
    </v-data-table>

    <template>
      <v-row justify="center">
        <v-dialog tile v-model="dialog" persistent max-width="60%">
          <v-card>
            <v-card-title class="headline">
              Edit Clarification Request
            </v-card-title>
            <v-card-text>
              <v-text-field
                v-model="newContent"
                label="Your request goes here."
                data-cy="inputNewContent"
              >
              </v-text-field>
            </v-card-text>
            <v-card-actions data-cy="actions">
              <v-spacer></v-spacer>
              <v-btn
                color="green darken-1"
                text
                @click="
                  dialog = false;
                  stopEditRequest();
                "
              >
                Cancel
              </v-btn>
              <v-btn
                color="green darken-1"
                text
                @click="
                  dialog = false;
                  editRequest();
                "
                data_cy="submitEdition"
              >
                Edit
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-row>
    </template>
    <v-dialog v-model="questionDialog" max-width="75%">
      <v-card>
        <v-card-title>
          <span class="headline">Question</span>
        </v-card-title>

        <v-card-text width="100%">
          <div v-if="questionDialog" class="question-context">
            <show-question :question="openQuestion" />
          </div>
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import ClarificationRequest from '@/models/clarification/ClarificationRequest';
import Question from '@/models/management/Question';
import ShowQuestion from '@/views/teacher/questions/ShowQuestion.vue';

@Component({
  components: { 'show-question': ShowQuestion }
})
export default class ClarificationsView extends Vue {
  requests: ClarificationRequest[] = [];
  search: string = '';
  newContent: string = '';
  editingItem: ClarificationRequest | null = null;
  dialog: boolean = false;
  questionDialog: boolean = false;
  openQuestion: Question | null = null;

  headers: object = [
    { text: 'Request', value: 'content', align: 'center', sortable: false },
    { text: 'Answer', value: 'answer', align: 'center', sortable: false },
    { text: 'Submission Date', value: 'creationDate', align: 'center' },
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

  showAnswer(request: ClarificationRequest): string | void {
    return request.getAnswerContent();
  }

  async showQuestionDialog(req: ClarificationRequest): Promise<void> {
    try {
      this.openQuestion = await RemoteServices.getQuestionById(
        req.getQuestionId()
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
      return;
    }

    this.questionDialog = true;
  }

  async deleteRequest(req: ClarificationRequest) {
    if (
      !confirm('Are you sure you want to delete this clarification request?')
    ) {
      return;
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

  startEditRequest(request: ClarificationRequest): void {
    this.editingItem = request;
    this.dialog = true;
  }

  stopEditRequest(): void {
    this.editingItem = null;
    this.newContent = '';
  }

  async editRequest() {
    await this.$store.dispatch('loading');
    if (this.editingItem) {
      this.editingItem.setContent(this.newContent);
      try {
        this.editingItem = await RemoteServices.editClarificationRequest(
          this.editingItem
        );
        this.stopEditRequest();
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
      await this.$store.dispatch('clearLoading');
    }
  }
}
</script>

<style scoped>
.question-context {
  text-align: left;
  margin-bottom: 20px;
}
</style>
