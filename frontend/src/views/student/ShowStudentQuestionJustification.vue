<template>
  <v-dialog
    :value="dialog"
    @input="$emit('close-show-student-question-justification', false)"
    @keydown.esc="$emit('close-show-student-question-justification', false)"
    max-width="75%"
  >
    <v-card>
      <v-card-title>
        <span class="headline">{{
          'Evaluation - ' + studentQuestion.title
        }}</span>
      </v-card-title>
      <v-card-text class="text-left">
        <div>
          <v-chip :color="studentQuestion.getEvaluationColor()" small>
            <span data-cy="showStatus">{{
              studentQuestion.submittedStatus
            }}</span>
          </v-chip>
        </div>
        <div style="margin: 1%;">
          <show-justification :studentQuestion="studentQuestion" />
        </div>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn
          dark
          color="blue darken-1"
          @click="$emit('close-show-student-question-justification')"
          data-cy="CancelEvaluation"
          >close</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model } from 'vue-property-decorator';
import StudentQuestion from '@/models/management/StudentQuestion';
import ShowJustification from '@/views/student/ShowJustification.vue';

@Component({
  components: {
    'show-justification': ShowJustification
  }
})
export default class ShowStudentQuestionJustification extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: StudentQuestion, required: true })
  readonly studentQuestion!: StudentQuestion;
}
</script>
