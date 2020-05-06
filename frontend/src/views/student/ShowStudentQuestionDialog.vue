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
        <show-student-question :studentQuestion="studentQuestion" />
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          dark
          color="blue darken-1"
          @click="$emit('close-show-student-question-dialog', false)"
          >close</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model } from 'vue-property-decorator';
import StudentQuestion from '@/models/management/StudentQuestion';
import ShowStudentQuestion from '@/views/student/ShowStudentQuestion.vue';

@Component({
  components: {
    'show-student-question': ShowStudentQuestion
  }
})
export default class ShowStudentQuestionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: StudentQuestion, required: true })
  readonly studentQuestion!: StudentQuestion;
}
</script>
