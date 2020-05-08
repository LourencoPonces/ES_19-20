<template>
  <div>
    <v-card class="discussion" style="margin-top: 30px;" outlined>
      <v-card-title class="title">
        <span>Clarification Requests</span>
        <v-spacer />
        <v-btn
          :disabled="this.alreadySubmitted()"
          color="primary"
          @click="newRequestButton()"
          data-cy="newRequest"
        >
          New Request
        </v-btn>
      </v-card-title>
      <v-divider></v-divider>
      <v-card-text v-if="hasClarificationRequests()">
        <v-expansion-panels focusable data-cy="questionRequests">
          <v-expansion-panel
            v-for="request in clarifications"
            :key="request.content"
          >
            <v-expansion-panel-header>
              <span class="multiline" data-cy="requestHeader">{{
                request.content
              }}</span>
            </v-expansion-panel-header>
            <v-expansion-panel-content v-if="request.hasMessages">
              <clarification-thread :request="request"></clarification-thread>
            </v-expansion-panel-content>
            <v-expansion-panel-content v-else>
              No answer available.
            </v-expansion-panel-content>
          </v-expansion-panel>
        </v-expansion-panels>
      </v-card-text>
      <v-card-text v-else>
        No requests available.
      </v-card-text>
    </v-card>
    <template>
      <v-row justify="center">
        <v-dialog tile v-model="creatingRequest" persistent max-width="60%">
          <v-card>
            <v-card-title class="headline">
              New Clarification Request
            </v-card-title>
            <v-card-text>
              <v-textarea
                v-model="requestContent"
                label="Your request goes here."
                data-cy="inputRequest"
              ></v-textarea>
            </v-card-text>
            <v-card-actions>
              <v-btn
                dark
                color="red"
                style="margin: 5px;"
                @click="cancelCreateRequest()"
              >
                Cancel
              </v-btn>
              <v-btn
                dark
                color="primary"
                style="margin: 5px;"
                @click="submitRequest()"
              >
                Submit
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-row>
    </template>
  </div>
</template>

<script lang="ts">
import { Vue, Prop, Emit, Component } from 'vue-property-decorator';
import StatementQuestion from '@/models/statement/StatementQuestion';
import ClarificationRequest from '@/models/clarification/ClarificationRequest';
import ClarificationThread from '../../ClarificationThread.vue';

@Component({
  components: {
    'clarification-thread': ClarificationThread
  }
})
export default class DiscussionComponent extends Vue {
  @Prop(StatementQuestion) readonly question!: StatementQuestion;
  @Prop({ type: Array }) readonly clarifications!: ClarificationRequest[];
  @Prop({ type: Array }) readonly userRequests!: ClarificationRequest[];

  creatingRequest: boolean = false;
  requestContent = '';

  newRequestButton(): void {
    this.creatingRequest = true;
  }

  cancelCreateRequest(): void {
    this.creatingRequest = false;
    this.requestContent = '';
  }

  hasClarificationRequests(): boolean {
    return this.clarifications.length > 0;
  }

  @Emit()
  submitRequest(): string[] {
    this.creatingRequest = false;
    let content = this.requestContent;
    this.requestContent = '';

    return [content, this.question.questionId.toString()];
  }

  alreadySubmitted(): boolean {
    let submitted = false;
    this.userRequests.forEach(req => {
      if (req.getQuestionId() == this.question.getQuestionId()) {
        submitted = true;
        return;
      }
    });
    return submitted;
  }
}
</script>

<style lang="scss" scoped>
.discussion {
  max-width: 1024px;
  perspective-origin: 512px 356.5px;
  transform-origin: 512px 356.5px;
  border: 0 none rgb(51, 51, 51);
  margin: 50px auto 150px;
  outline: rgb(51, 51, 51) none 0;
  overflow: hidden;
  top: -100px;
  letter-spacing: 0 !important;
  vertical-align: middle;

  v-card-title {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    text-align: left;
    text-decoration: none solid rgb(51, 51, 51);
    text-size-adjust: 100%;
  }
}

.multiline {
  white-space: pre;
}
</style>
