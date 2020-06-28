<template>
  <!-- BROWSER -->
  <v-dialog
    v-if="!isMobile"
    :value="dialog"
    @input="$emit('dialog', false)"
    @keydown.esc="$emit('dialog', false)"
    max-width="75%"
  >
    <v-data-table
      :headers="headers"
      :items="quizAnswers"
      :search="search"
      disable-pagination
      :hide-default-footer="true"
      :mobile-breakpoint="0"
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
          <span v-if="timeToSubmission > 0">{{ getTimeAsHHMMSS }}</span>
        </v-card-title>
      </template>

      <template v-slot:item.answers="{ item }">
        <td
          v-for="questionAnswer in item.questionAnswers"
          :key="questionAnswer.question.id"
          v-bind:class="[
            questionAnswer.option.correct ? 'green' : 'red darken-4'
          ]"
          style="border: 0"
        >
          {{ convertToLetter(questionAnswer.option.sequence) }}
        </td>
        <template v-if="item.questionAnswers.length === 0">
          <td v-for="i in correctSequence.length" :key="i" style="border: 0">
            X
          </td>
        </template>
      </template>

      <template v-slot:body.append>
        <tr>
          <td colspan="4">
            Correct key:
          </td>
          <div>
            <td></td>
            <td
              v-for="(sequence, index) in correctSequence"
              :key="index"
              style="border: 0"
            >
              {{ convertToLetter(sequence) }}
            </td>
          </div>
        </tr>
      </template>
    </v-data-table>
  </v-dialog>
  <!-- MOBILE -->
  <v-dialog
    v-else
    :value="dialog"
    @input="$emit('dialog', false)"
    @keydown.esc="$emit('dialog', false)"
    max-width="100%"
  >
    <v-data-table
      :headers="headers_mobile"
      :items="quizAnswers"
      :search="search"
      :sort-by="['name']"
      sort-asc
      disable-pagination
      :hide-default-footer="true"
      :mobile-breakpoint="0"
      style="margin-top: 25px"
    >
      <template v-slot:top>
        <v-card-title>
          <v-text-field
            v-model="search"
            append-icon="search"
            label="Search"
            class="mx-2"
          />
        </v-card-title>
      </template>
      <template v-slot:item.answers="{ item }">
        <v-row @click="showAnswerDialogMobile(item)">
          <v-col
            v-for="questionAnswer in item.questionAnswers"
            :key="questionAnswer.question.id"
            v-bind:class="[
              questionAnswer.option.correct ? 'green' : 'red darken-4'
            ]"
            style="border: 0"
          >
            {{ convertToLetter(questionAnswer.option.sequence) }}
          </v-col>
          <template v-if="item.questionAnswers.length === 0">
            <v-col
              v-for="i in correctSequence.length"
              :key="i"
              style="border: 0"
            >
              X
            </v-col>
          </template>
        </v-row>
      </template>
    </v-data-table>
    <show-quiz-answer-dialog-mobile
      v-if="current_answer"
      v-model="answerDialogMobile"
      :answer="current_answer"
    />
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model, Watch } from 'vue-property-decorator';
import { QuizAnswer } from '@/models/management/QuizAnswer';
import { QuizAnswers } from '@/models/management/QuizAnswers';
import ShowQuizAnswerDialogMobile from '@/views/teacher/quizzes/ShowQuizAnswerDialogMobile.vue';

@Component({
  components: {
    'show-quiz-answer-dialog-mobile': ShowQuizAnswerDialogMobile
  }
})
export default class ShowQuizAnswersDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ required: true }) readonly quizAnswers!: QuizAnswer[];
  @Prop({ required: true }) readonly correctSequence!: number[];
  @Prop({ required: true }) readonly timeToSubmission!: number;
  @Prop({ type: Boolean, required: true }) readonly isMobile!: boolean;

  secondsLeft: number = 0;
  search: string = '';
  timeout: number | null = null;
  current_answer: QuizAnswer | null = null;
  answerDialogMobile: boolean = false;

  @Watch('timeToSubmission')
  updateTimer() {
    if (this.timeToSubmission > 0) {
      this.secondsLeft = this.timeToSubmission;
      if (this.timeout) {
        clearTimeout(this.timeout);
      }
      this.countDownTimer();
    }
  }

  headers_mobile: object = [
    {
      text: 'Answers',
      value: 'answers',
      align: 'center',
      width: '25%',
      sortable: false
    }
  ];

  headers: object = [
    { text: 'Name', value: 'name', align: 'left', width: '5%' },
    {
      text: 'Username',
      value: 'username',
      align: 'center',
      width: '5%'
    },
    {
      text: 'Start Date',
      value: 'creationDate',
      align: 'center',
      width: '5%'
    },
    {
      text: 'Answer Date',
      value: 'answerDate',
      align: 'center',
      width: '5%'
    },
    {
      text: 'Answers',
      value: 'answers',
      align: 'center',
      width: '5%'
    }
  ];

  countDownTimer() {
    if (this.secondsLeft >= 0) {
      this.secondsLeft -= 1;
      this.timeout = setTimeout(() => {
        this.countDownTimer();
      }, 1000);
    }
  }

  get getTimeAsHHMMSS() {
    let hours = Math.floor(this.secondsLeft / 3600);
    let minutes = Math.floor((this.secondsLeft - hours * 3600) / 60);
    let seconds = this.secondsLeft - hours * 3600 - minutes * 60;

    let hoursString = hours < 10 ? '0' + hours : hours;
    let minutesString = minutes < 10 ? '0' + minutes : minutes;
    let secondsString = seconds < 10 ? '0' + seconds : seconds;

    return `${hoursString}:${minutesString}:${secondsString}`;
  }

  convertToLetter(number: number) {
    if (number === undefined) {
      return 'X';
    } else {
      return String.fromCharCode(65 + number);
    }
  }

  showAnswerDialogMobile(answer) {
    this.current_answer = answer;
    this.answerDialogMobile = true;
  }
}
</script>
