<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :custom-filter="customFilter"
      :items="studentQuestions"
      :search="search"
      multi-sort
      :sort-by="['creationDate']"
      :sort-desc="[true]"
      :mobile-breakpoint="0"
      :items-per-page="15"
      :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
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

      <template v-slot:item.content="{ item }">
        <p @click="showQuestionDialog(item)"
      /></template>

      <template v-slot:item.topics="{ item }">
        <v-chip-group>
          <v-chip v-for="topic in item.topics" :key="topic.name">
            {{ topic.name }}
          </v-chip>
        </v-chip-group>
      </template>

      <template
        v-slot:item.submittedStatus="{ item }"
        data-cy="submitted-status"
      >
        <v-chip
          :color="item.getEvaluationColor()"
          small
          @click="showEvaluateStudentQuestionDialog(item)"
          data-cy="evaluate"
        >
          <span>{{ item.submittedStatus }}</span>
        </v-chip>
      </template>

      <template v-slot:item.justification="{ item }">
        <span margin="5%">{{ item.justification }}</span>
      </template>

      <template v-slot:item.image="{ item }">
        <v-file-input
          show-size
          dense
          small-chips
          @change="handleFileUpload($event, item)"
          accept="image/*"
        />
      </template>

      <template v-slot:item.action="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="showQuestionDialog(item)"
              >visibility</v-icon
            >
          </template>
          <span>Show Question</span>
        </v-tooltip>
      </template>
    </v-data-table>
    <show-question-dialog
      v-if="currentQuestion"
      v-model="questionDialog"
      :question="currentQuestion"
      v-on:close-show-question-dialog="onCloseShowQuestionDialog"
    />
    <evaluate-question-dialog
      v-if="currentQuestion"
      v-model="evaluateQuestion"
      :studentQuestion="currentQuestion"
      v-on:evaluated-question="onEvaluatedQuestion"
      v-on:cancel-evaluate="onCancelEvaluation"
    />
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Question from '@/models/management/Question';
import Image from '@/models/management/Image';
import Topic from '@/models/management/Topic';
import ShowQuestionDialog from '@/views/teacher/questions/ShowQuestionDialog.vue';
import EvaluateQuestionDialog from '@/views/teacher/questions/EvaluateStudentQuestionDialog.vue';
import StudentQuestion from '@/models/management/StudentQuestion';

@Component({
  components: {
    'show-question-dialog': ShowQuestionDialog,
    'evaluate-question-dialog': EvaluateQuestionDialog
  }
})
export default class StudentQuestionsView extends Vue {
  studentQuestions: StudentQuestion[] = [];
  topics: Topic[] = [];
  currentQuestion: StudentQuestion | null = null;
  evaluateQuestion: boolean = false;
  questionDialog: boolean = false;
  search: string = '';

  headers: object = [
    { text: 'Title', value: 'title', align: 'center' },
    { text: 'Question', value: 'content', align: 'left' },
    {
      text: 'Topics',
      value: 'topics',
      align: 'center',
      sortable: false
    },
    { text: 'Submitted Status', value: 'submittedStatus', align: 'center' },
    { text: 'Justification', value: 'justification', align: 'center' },
    {
      text: 'Creation Date',
      value: 'creationDate',
      align: 'center'
    },
    {
      text: 'Image',
      value: 'image',
      align: 'center',
      sortable: false
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'center',
      sortable: false
    }
  ];

  @Watch('evaluateQuestion')
  closeError() {
    if (!this.evaluateQuestion) {
      this.currentQuestion = null;
    }
  }

  async created() {
    await this.$store.dispatch('loading');
    try {
      [this.topics, this.studentQuestions] = await Promise.all([
        RemoteServices.getTopics(),
        RemoteServices.getSubmittedStudentQuestions()
      ]);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  customFilter(value: string, search: string, question: Question) {
    // noinspection SuspiciousTypeOfGuard,SuspiciousTypeOfGuard
    return (
      search != null &&
      JSON.stringify(question)
        .toLowerCase()
        .indexOf(search.toLowerCase()) !== -1
    );
  }

  async handleFileUpload(event: File, question: Question) {
    if (question.id) {
      try {
        const imageURL = await RemoteServices.uploadImage(event, question.id);
        question.image = new Image();
        question.image.url = imageURL;
        confirm('Image ' + imageURL + ' was uploaded!');
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  showQuestionDialog(question: StudentQuestion) {
    this.currentQuestion = question;
    this.questionDialog = true;
  }

  onCloseShowQuestionDialog() {
    this.questionDialog = false;
  }

  async showEvaluateStudentQuestionDialog(sq: StudentQuestion) {
    this.currentQuestion = sq;
    this.evaluateQuestion = true;
    return;
  }

  onCancelEvaluation() {
    this.evaluateQuestion = false;
  }

  onEvaluatedQuestion(studentQuestion: StudentQuestion) {
    // update evaluated question
    this.studentQuestions.forEach(sq => {
      if (sq.id === studentQuestion.id) {
        sq.justification = studentQuestion.justification;
        sq.submittedStatus = studentQuestion.submittedStatus;
      }
    });
    this.evaluateQuestion = false;
    this.currentQuestion = null;
  }
}
</script>

<style lang="scss" scoped>
.question-textarea {
  text-align: left;

  .CodeMirror,
  .CodeMirror-scroll {
    min-height: 200px !important;
  }
}
.option-textarea {
  text-align: left;

  .CodeMirror,
  .CodeMirror-scroll {
    min-height: 100px !important;
  }
}
</style>
