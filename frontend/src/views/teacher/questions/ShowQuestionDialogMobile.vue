<template>
  <v-dialog
    :value="dialog"
    @input="$emit('close-show-question-dialog', false)"
    @keydown.esc="$emit('close-show-question-dialog', false)"
    max-width="75%"
  >
    <v-card>
      <v-card-title class="justify-center">
        <span>
          {{ question.title }}<br />
          <span class="subtitle">{{ question.creationDate }} </span></span
        >
      </v-card-title>

      <v-card-text class="text-left">
        <v-row>
          <v-col cols="4">
            <b>Status: </b>
          </v-col>
          <v-col>
            <v-select
              v-model="question.status"
              :items="statusList"
              dense
              @change="$emit('set-status', question.id, question.status)"
            >
              <template v-slot:selection="{ item }">
                <v-chip :color="statusColor" small>
                  <span>{{ item }}</span>
                </v-chip>
              </template>
            </v-select>
          </v-col>
        </v-row>
        <v-expansion-panels focusable>
          <v-expansion-panel>
            <v-expansion-panel-header>
              <p>Topics</p>
            </v-expansion-panel-header>
            <v-expansion-panel-content>
              <edit-question-topics
                :question="question"
                :topics="topics"
                v-on:question-changed-topics="changeTopics"
              />
            </v-expansion-panel-content>
          </v-expansion-panel>
          <v-expansion-panel>
            <v-expansion-panel-header>
              <p>Question</p>
            </v-expansion-panel-header>
            <v-expansion-panel-content>
              <show-question :question="question" />
              <template>
                <v-btn fab primary small color="primary" align-self="right">
                  <v-icon
                    medium
                    class="mr-2"
                    @click="$emit('edit-question', question)"
                    >edit</v-icon
                  >
                </v-btn>
              </template>
            </v-expansion-panel-content>
          </v-expansion-panel>
          <v-expansion-panel v-if="question.difficulty">
            <v-expansion-panel-header>
              <p>Stats</p>
            </v-expansion-panel-header>
            <v-expansion-panel-content>
              <v-row>
                <v-col cols="8">
                  <span>Difficulty:</span>
                </v-col>
                <v-col align-self="center">
                  <v-chip :color="diffColor" dark>
                    {{ question.difficulty + '%' }}</v-chip
                  >
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="9">
                  <span>No. of answers:</span>
                </v-col>
                <v-col align-self="center">
                  <span>{{ question.numberOfAnswers }}</span>
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="9">
                  <span>No. of generated quizzes:</span>
                </v-col>
                <v-col align-self="center">
                  <span>{{ question.numberOfGeneratedQuizzes }}</span>
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="9">
                  <span>No. of non generated quizzes:</span>
                </v-col>
                <v-col align-self="center">
                  <span>{{ question.numberOfNonGeneratedQuizzes }}</span>
                </v-col>
              </v-row>
            </v-expansion-panel-content>
          </v-expansion-panel>
        </v-expansion-panels>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn
          v-if="question.numberOfAnswers == 0"
          fab
          color="error"
          small
          @click="$emit('delete-question', question)"
        >
          <v-icon medium class="mr-2">
            delete
          </v-icon>
        </v-btn>
        <v-btn
          fab
          color="primary"
          small
          @click="$emit('duplicate-question', question)"
        >
          <v-icon medium class="mr-2">
            far fa-copy
          </v-icon>
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model } from 'vue-property-decorator';
import Question from '@/models/management/Question';
import Topic from '@/models/management/Topic';
import ShowQuestion from '@/views/teacher/questions/ShowQuestion.vue';
import EditQuestionTopics from '@/views/teacher/questions/EditQuestionTopics.vue';

@Component({
  components: {
    'show-question': ShowQuestion,
    'edit-question-topics': EditQuestionTopics
  }
})
export default class ShowQuestionDialogMobile extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Question, required: true }) readonly question!: Question;
  @Prop({ type: Array, required: true }) readonly topics: Topic[];
  @Prop({ type: String, required: true }) readonly statusColor!: String;
  @Prop({ type: String, required: true }) readonly diffColor!: String;
  statusList = ['DISABLED', 'AVAILABLE', 'REMOVED'];

  changeTopics(questionId: Number, changedTopics: Topic[]) {
    this.$emit('question-changed-topics', questionId, changedTopics);
  }
}
</script>
<style>
.subtitle {
  color: gray;
  font-size: 11px;
}
</style>
