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
        <v-expansion-panels focusable>
          <v-expansion-panel>
            <v-expansion-panel-header>
              <p>Topics</p>
            </v-expansion-panel-header>
            <v-expansion-panel-content>
              <v-list>
                <v-list-item v-for="topic in question.topics" :key="topic.id">
                  <v-list-item-title>{{ topic.name }}</v-list-item-title>
                </v-list-item>
              </v-list>
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
          <v-expansion-panel v-if="question.difficulty">
            <v-expansion-panel-header>
              <p>Stats</p>
            </v-expansion-panel-header>
            <v-expansion-panel-content>
              <v-row>
                <v-col cols="8">
                  <span>Difficulty:</span>
                </v-col>
                <v-col cols="4" align-self="center">
                  {{ question.difficulty + '%' }}
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
        <v-row v-if="question.sequence">
          <v-col align-self="center">
            <b>Sequence:</b>
          </v-col>
          <v-col>
            <v-select
              v-model="new_sequence"
              :items="possible_sequences"
              dense
              @change="
                $emit('change-question-position', question, new_sequence - 1)
              "
            >
            </v-select>
          </v-col>
        </v-row>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn v-if="!question.sequence" fab small class="mr-2" color="primary">
          <v-icon medium class="mr-2" @click="$emit('add-to-quiz', question)">
            add</v-icon
          >
        </v-btn>
        <v-btn v-else fab small class="mr-2" color="red">
          <v-icon
            medium
            class="mr-2"
            @click="$emit('remove-from-quiz', question)"
          >
            remove</v-icon
          >
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
export default class ShowQuizQuestionDialogMobile extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Question, required: true }) readonly question!: Question;
  @Prop({ type: Number, required: true }) readonly number_questions!: Number;
  possible_sequences: Number[] = [];
  new_sequence!: number;

  created() {
    for (let i = 0; i < this.number_questions; i++) {
      this.possible_sequences.push(i + 1);
    }
    this.new_sequence = this.question.sequence;
  }
}
</script>
<style>
.subtitle {
  color: gray;
  font-size: 11px;
}
</style>
