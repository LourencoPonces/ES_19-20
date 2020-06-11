<template>
  <v-dialog
    :value="dialog"
    @input="$emit('close-show-question-dialog', false)"
    @keydown.esc="$emit('close-show-question-dialog', false)"
    max-width="75%"
  >
    <v-card>
      <v-card-title>
        <span class="headline">
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
                <v-chip :color="color" small>
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
            </v-expansion-panel-content>
          </v-expansion-panel>
        </v-expansion-panels>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          dark
          color="blue darken-1"
          @click="$emit('close-show-question-dialog')"
        >
          close
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
  @Prop({ type: String, required: true }) readonly color!: String;
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
