<template>
  <v-dialog
    :value="dialog"
    @input="$emit('dialog', false)"
    @keydown.esc="$emit('dialog', false)"
    max-width="75%"
    max-height="60%"
  >
    <v-card>
      <v-card-title>
        <span class="headline">
          {{ 'Evaluate: ' + this.evalQuestion.title }}
        </span>
      </v-card-title>

      <v-card-text class="text-left" v-if="evalQuestion">
        <v-container grid-list-md fluid>
          <v-layout column wrap>
            <v-select
              v-model="evalQuestion.submittedStatus"
              :items="statusList"
              :reduce="label => label.code"
              :label="`Status`"
            >
              <template v-slot:selection="{ item }">
                <v-chip :color="getEvaluationColor(item)" small>
                  <span>{{ item }}</span>
                </v-chip>
              </template>
            </v-select>
            <v-textarea
              outline
              rows="3"
              v-model="evalQuestion.justification"
              :label="`Justification`"
            ></v-textarea>
          </v-layout>
        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn color="blue darken-1" @click="$emit('cancel-evaluate', false)"
          >Cancel</v-btn
        >
        <v-btn color="blue darken-1" @click="evaluateQuestion">Evaluate</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import StudentQuestion from '@/models/management/StudentQuestion';

@Component
export default class EvaluateQuestionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: StudentQuestion, required: true })
  readonly studentQuestion!: StudentQuestion;

  statusList = ['Waiting for Approval', 'Approved', 'Rejected'];

  evalQuestion!: StudentQuestion;

  created() {
    this.evalQuestion = new StudentQuestion(this.studentQuestion);
    this.evalQuestion.justification = '';
  }

  async evaluateQuestion() {
    if (this.evalQuestion.submittedStatus === 'Waiting for Approval') {
      await this.$store.dispatch(
        'error',
        'You must approve or reject the question'
      );
      return;
    } else if (
      this.evalQuestion.submittedStatus === 'Rejected' &&
      this.evalQuestion.justification.trim() === ''
    ) {
      await this.$store.dispatch(
        'error',
        'Rejected questions must be justified'
      );
      return;
    }
    try {
      const result = await RemoteServices.evaluateStudentQuestion(
        this.evalQuestion.id || 0,
        this.evalQuestion.submittedStatus,
        this.evalQuestion.justification
      );
      this.$emit('evaluated-question', result);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  getEvaluationColor(status: string) {
    if (status === 'Rejected') return 'red';
    else if (status === 'Waiting for Approval') return 'orange';
    else return 'green';
  }
}
</script>
