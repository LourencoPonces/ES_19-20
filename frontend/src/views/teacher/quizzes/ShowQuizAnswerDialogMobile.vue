<template>
  <v-dialog :value="dialog" @input="$emit('dialog', false)" max-width="100%">
    <v-card>
      <v-card-title class="justify-center">
        <span>{{ 'Quiz Answer' }}<br /></span>
      </v-card-title>

      <v-card-text class="text-left">
        <v-row>
          <v-col cols="4">
            <b>Name:</b>
          </v-col>
          <v-col>{{ answer.name }}</v-col>
        </v-row>
        <v-row>
          <v-col cols="4">
            <b>Username:</b>
          </v-col>
          <v-col>{{ answer.username }}</v-col>
        </v-row>
        <v-row>
          <v-col cols="4">
            <b>Answered:</b>
          </v-col>
          <v-col>{{ answer.answerDate }}</v-col>
        </v-row>
        <v-row>
          <v-col cols="4">
            <b>Answer:</b>
          </v-col>
          <v-col
            v-for="questionAnswer in answer.questionAnswers"
            :key="questionAnswer.question.id"
            v-bind:class="[
              questionAnswer.option.correct ? 'green' : 'red darken-4'
            ]"
            style="border: 0"
          >
            {{ convertToLetter(questionAnswer.option.sequence) }}
          </v-col>
          <v-col v-if="answer.questionAnswers.length === 0">{{ 'None' }}</v-col>
        </v-row>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model } from 'vue-property-decorator';
import { QuizAnswer } from '@/models/management/QuizAnswer';

@Component
export default class ShowQuizAnswerDialogMobile extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: QuizAnswer, required: true }) readonly answer!: QuizAnswer;

  convertToLetter(number: number) {
    if (number === undefined) {
      return 'X';
    } else {
      return String.fromCharCode(65 + number);
    }
  }
}
</script>
