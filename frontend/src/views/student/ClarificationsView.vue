<template>
  <div class="container">
    <h2>Clarification Requests</h2>
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
              single-line
              hide-details
            />

            <v-spacer />
          </v-card-title>
        </template>

        <template v-slot:item.content="{ item }">
          <span style="white-space: pre;">{{ item.content }}</span>
        </template>

        <template v-slot:item.answer="{ item }">
          <span style="white-space: pre;">{{ showAnswer(item) }}</span>
        </template>

        <template v-slot:item.action="{ item }">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                v-if="!item.hasAnswer"
                small
                class="mr-2"
                v-on="on"
                @click="startEditRequest(item)"
                data-cy="edit"
                >edit</v-icon
              >
              <v-icon v-else disabled small class="mr-2" data-cy="editDisabled">
                edit
              </v-icon>
            </template>
            <span>Edit Request</span>
          </v-tooltip>
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                v-if="!item.hasAnswer"
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
                color="red"
                data-cy="deleteDisabled"
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
                <v-textarea
                  v-model="newContent"
                  label="Your request goes here."
                  data-cy="inputNewContent"
                >
                </v-textarea>
              </v-card-text>
              <v-card-actions data-cy="actions">
                <v-spacer></v-spacer>
                <v-btn
                  dark
                  color="red"
                  @click="
                    dialog = false;
                    stopEditRequest();
                  "
                >
                  Cancel
                </v-btn>
                <v-btn
                  dark
                  color="primary"
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
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import ClarificationRequest from '@/models/clarification/ClarificationRequest';
import Question from '@/models/management/Question';

@Component
export default class ClarificationsView extends Vue {
  requests: ClarificationRequest[] = [];
  search: string = '';
  newContent: string = '';
  editingItem: ClarificationRequest | null = null;
  dialog: boolean = false;

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
    this.newContent = request.getContent();
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
