<template>
  <v-card class="discussion" :max-height="270" style="margin-top: 30px;" outlined>
      <v-card-title>
        Clarification Requests
         <v-btn v-if="!creatingRequest" class="add-button" dark color="primary" @click="newRequestButton()">New Request</v-btn>
      </v-card-title>
     
      <v-divider></v-divider>

      <v-card-text v-if="creatingRequest">
        <v-text-field v-model="requestContent" label="Your request goes here."></v-text-field>
        <v-btn dark color="red" style="margin: 5px;" @click="cancelCreateRequest()">Cancel</v-btn>
        <v-btn dark color="primary" style="margin: 5px;" @click="submitRequest()">Submit</v-btn>
      </v-card-text>

      <v-card-text v-else-if="hasClarificationRequests()">
        <v-expansion-panels focusable>
          <v-expansion-panel
            v-for="(request,i) in clarifications.length"
            :key="i"
          >
            <v-expansion-panel-header>{{clarifications[i].content}}</v-expansion-panel-header>
            <v-expansion-panel-content v-if="clarifications[i].hasAnswer()">
              {{clarifications[i].answer}}
            </v-expansion-panel-content>
            <v-expansion-panel-content v-else>
              No answer available.
            </v-expansion-panel-content>
          </v-expansion-panel>
        </v-expansion-panels>
      </v-card-text>

      <v-card-text v-else>No requests available.</v-card-text>
      
    </v-card>
</template>

<script lang="ts">
import { Vue, Prop, Emit, Component } from 'vue-property-decorator';
import StatementQuestion from '@/models/statement/StatementQuestion';
import ClarificationRequest from '@/models/clarification/ClarificationRequest';

@Component
export default class DiscussionComponent extends Vue {
  @Prop(StatementQuestion) question!: StatementQuestion;
  @Prop({type: Array}) readonly clarifications!: ClarificationRequest[]

  creatingRequest: boolean = false;
  requestContent = '';
  nRequests!: number;

  newRequestButton() {
    this.creatingRequest = true;
  }

  cancelCreateRequest() {
    this.creatingRequest = false;
    this.requestContent = '';
  }

  hasClarificationRequests() {
    return this.clarifications.length > 0
  }

  @Emit()
  submitRequest() {
    
    this.creatingRequest = false;
    let content = this.requestContent;
    this.requestContent = '';

    return [content, this.question.questionId.toString()];
  }
}
</script>

<style lang="scss" scoped>
.discussion {
  margin: 17.5%;
  width: 65%;
  top: -15%;
  align-self: center;

  .add-button {
    position: relative;
    right: -638px;
  }
}
</style>
