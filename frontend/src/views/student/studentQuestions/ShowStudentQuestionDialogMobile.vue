<template>
  <v-dialog
    :value="dialog"
    @input="$emit('close-show-student-question-dialog', false)"
    @keydown.esc="$emit('close-show-student-question-dialog', false)"
    max-width="75%"
  >
    <v-card>
      <v-card-title>
        <span class="headline">{{ studentQuestion.title }}</span>
      </v-card-title>

      <v-card-text class="text-left">
        <v-expansion-panels focusable>
          <v-expansion-panel>
            <v-expansion-panel-header>Status</v-expansion-panel-header>
            <v-expansion-panel-content>
              <v-row>
                <v-chip
                  class="evalChip"
                  :color="studentQuestion.getEvaluationColor()"
                  small
                >
                  <span>{{ studentQuestion.submittedStatus }}</span>
                </v-chip>
              </v-row>
              <v-row>
                <show-justification :studentQuestion="studentQuestion" />
              </v-row>
            </v-expansion-panel-content>
          </v-expansion-panel>
          <v-expansion-panel>
            <v-expansion-panel-header>Topics</v-expansion-panel-header>
            <v-expansion-panel-content>
              <v-list
                dense
                v-for="topic in studentQuestion.topics"
                v-bind:key="topic.id"
              >
                <v-list-item>
                  <v-list-item-title>{{ topic.name }}</v-list-item-title>
                </v-list-item>
              </v-list>
            </v-expansion-panel-content>
          </v-expansion-panel>
          <v-expansion-panel>
            <v-expansion-panel-header>Question</v-expansion-panel-header>
            <v-expansion-panel-content>
              <show-student-question :studentQuestion="studentQuestion" />
            </v-expansion-panel-content>
          </v-expansion-panel>
        </v-expansion-panels>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          v-if="studentQuestion.isChangeable()"
          fab
          primary
          small
          color="primary"
          @click="$emit('edit-student-question-mobile', studentQuestion)"
        >
          <v-icon medium class="mr-2">edit</v-icon>
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model } from 'vue-property-decorator';
import StudentQuestion from '@/models/management/StudentQuestion';
import ShowStudentQuestion from '@/views/student/studentQuestions/ShowStudentQuestion.vue';
import ShowJustification from '@/views/student/studentQuestions/ShowJustification.vue';

@Component({
  components: {
    'show-student-question': ShowStudentQuestion,
    'show-justification': ShowJustification
  }
})
export default class ShowStudentQuestionDialogMobile extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: StudentQuestion, required: true })
  readonly studentQuestion!: StudentQuestion;
}
</script>
<style lang="scss" scoped>
.v-list {
  padding: 0px;
}

.evalChip {
  margin-top: 16px;
  margin-bottom: 16px;
}
</style>
