<template>
  <v-dialog
    :value="dialog"
    @input="$emit('dialog', false)"
    @keydown.esc="$emit('dialog', false)"
    max-width="75%"
  >
    <v-card v-if="quiz">
      <v-card-title >{{ quiz.title }}</v-card-title>

      <v-card-text>
        <v-row>
          <v-col cols="5" align-self="center">
            <b>Options:</b>
          </v-col>
          <v-col>
            <v-spacer />
              <v-icon v-if="quiz.oneWay" class="mr-2">forward</v-icon>
              <v-icon v-if="quiz.scramble" class="mr-2">shuffle</v-icon>
              <v-icon v-if="quiz.qrCodeOnly" class="mr-2">fas fa-qrcode</v-icon>
          </v-col>
        </v-row>
        <v-expansion-panels focusable>
          <v-expansion-panel>
            <v-expansion-panel-header>Dates</v-expansion-panel-header>
            <v-expansion-panel-content>
              <v-row>
                <v-col>
                  <span><b>Created:</b></span>
                </v-col>
                <v-col>
                  {{  quiz.creationDate  }}
                </v-col>
              </v-row>
              <v-row v-if="quiz.availableDate">
                <v-col>
                  <span><b>Available:</b></span>
                </v-col>
                <v-col>
                  {{ quiz.availableDate }}
                </v-col>
              </v-row>
              <v-row v-if="quiz.conclusionDate">
                <v-col>
                  <span><b>Conclusion:</b></span>
                </v-col>
                <v-col>
                  {{ quiz.conclusionDate }}
                </v-col>
              </v-row>
              <v-row v-if="quiz.resultsDate">
                <v-col>
                  <span><b>Results:</b></span>
                </v-col>
                <v-col>
                  {{ quiz.resultsDate }}
                </v-col>
              </v-row>
            </v-expansion-panel-content>
          </v-expansion-panel>
          <v-expansion-panel>
            <v-expansion-panel-header>Stats</v-expansion-panel-header>
            <v-expansion-panel-content>
              <v-row>
                <v-col>
                  <span><b>Number of Questions:</b></span>
                </v-col>
                <v-col>
                  {{ quiz.numberOfQuestions }}
                </v-col>
              </v-row>
              <v-row>
                <v-col>
                  <span><b>Number of Answers:</b></span>
                </v-col>
                <v-col>
                  {{ quiz.numberOfAnswers }}
                </v-col>
              </v-row>
            </v-expansion-panel-content>
          </v-expansion-panel>
          <v-expansion-panel>
            <v-expansion-panel-header>Questions</v-expansion-panel-header>
            <v-expansion-panel-content>
              <show-question-list :questions="quiz.questions" />
            </v-expansion-panel-content>
          </v-expansion-panel>
        </v-expansion-panels>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          fab
          color="error"
          small
          @click="$emit('delete-quiz', quiz.id)"
        >
          <v-icon medium class="mr-2">
            delete
          </v-icon>
        </v-btn>
        <v-btn
          v-if="quiz.qrCodeOnly"
          fab
          primary
          small
          color="primary"
          @click="$emit('show-qr-code', quiz.id)"
        >
          <v-icon class="mr-2">fas fa-qrcode</v-icon>
        </v-btn>
        <v-btn
          v-if="quiz.numberOfAnswers > 0"
          fab
          primary
          small
          color="primary"
          @click="$emit('show-quiz-answers', quiz.id)"
        >
          <v-icon medium class="mr-2">mdi-table</v-icon>
        </v-btn>
        <v-btn
          fab
          primary
          small
          color="primary"
          @click="$emit('edit-quiz', quiz, $event)"
        >
          <v-icon medium class="mr-2">edit</v-icon>
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model } from 'vue-property-decorator';
import { Quiz } from '@/models/management/Quiz';
import ShowQuestionList from '@/views/teacher/questions/ShowQuestionList.vue';

@Component({
  components: {
    'show-question-list': ShowQuestionList
  }
})
export default class ShowQuizDialogMobile extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Quiz, required: true }) readonly quiz!: Quiz | null;

}
</script>
