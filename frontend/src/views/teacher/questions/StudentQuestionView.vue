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

      <template v-slot:item.title="{ item }">
        <p
          style="cursor: pointer"
          @click="showQuestionDialog(item)"
          @contextmenu="editStudentQuestion(item, $event)"
        >
          {{ item.title }}
        </p>
      </template>

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
        <p
          @click="showEvaluateStudentQuestionDialog(item)"
          style="cursor:pointer"
          data-cy="showJustification"
        >
          {{ truncate(item.justification) }}
        </p>
      </template>

      <template v-slot:item.action="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              large
              class="mr-2"
              v-on="on"
              @click="showQuestionDialog(item)"
              >visibility</v-icon
            >
          </template>
          <span>Show Question</span>
        </v-tooltip>
        <v-tooltip bottom v-if="item.canEvaluate()">
          <template v-slot:activator="{ on }">
            <v-icon
              large
              class="mr-2"
              v-on="on"
              @click="showEvaluateStudentQuestionDialog(item)"
              >fa-clipboard-check</v-icon
            >
          </template>
          <span>Evaluate</span>
        </v-tooltip>
        <v-tooltip bottom v-if="item.canEvaluate()">
          <template v-slot:activator="{ on }">
            <v-icon
              large
              class="mr-2"
              v-on="on"
              @click="editStudentQuestion(item)"
              data-cy="editStudentQuestion"
              >edit</v-icon
            >
          </template>
          <span>Edit and Promote Question</span>
        </v-tooltip>
      </template>
    </v-data-table>
    <footer>
      <v-icon class="mr-2">mouse</v-icon>Left-click on question's title to view
      it. <v-icon class="mr-2">mouse</v-icon>Right-click on question's title to
      edit it.
    </footer>
    <show-question-dialog
      v-if="questionDialog"
      v-model="questionDialog"
      :question="currentStudentQuestion"
      v-on:close-show-question-dialog="onCloseDialog"
    />
    <evaluate-question-dialog
      v-if="evaluateQuestion"
      v-model="evaluateQuestion"
      :studentQuestion="currentStudentQuestion"
      v-on:evaluated-question="onSaveStudentQuestion"
      v-on:cancel-evaluate="onCloseDialog"
    />
    <edit-and-promote-question-dialog
      v-if="editAndPromoteStudentQuestionDialog"
      v-model="editAndPromoteStudentQuestionDialog"
      :studentQuestion="currentStudentQuestion"
      :topics="topics"
      v-on:save-student-question="onSaveStudentQuestion"
      v-on:cancel-evaluate="onCloseDialog"
    />
    <show-student-question-justification
      v-if="studentQuestionJustification"
      v-model="studentQuestionJustification"
      :studentQuestion="currentStudentQuestion"
      v-on:close-show-student-question-justification="onCloseDialog"
    />
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Question from '@/models/management/Question';
import Topic from '@/models/management/Topic';
import ShowQuestionDialog from '@/views/teacher/questions/ShowQuestionDialog.vue';
import EvaluateQuestionDialog from '@/views/teacher/questions/EvaluateStudentQuestionDialog.vue';
import StudentQuestion from '@/models/management/StudentQuestion';
import EditAndPromoteStudentQuestionDialog from '@/views/teacher/questions/EditAndPromoteStudentQuestionDialog.vue';
import ShowStudentQuestionJustification from '@/views/student/ShowStudentQuestionJustification.vue';

@Component({
  components: {
    'show-question-dialog': ShowQuestionDialog,
    'evaluate-question-dialog': EvaluateQuestionDialog,
    'edit-and-promote-question-dialog': EditAndPromoteStudentQuestionDialog,
    'show-student-question-justification': ShowStudentQuestionJustification
  }
})
export default class StudentQuestionsView extends Vue {
  studentQuestions: StudentQuestion[] = [];
  topics: Topic[] = [];
  currentStudentQuestion: StudentQuestion | null = null;
  evaluateQuestion: boolean = false;
  questionDialog: boolean = false;
  editAndPromoteStudentQuestionDialog: boolean = false;
  studentQuestionJustification: boolean = false;
  search: string = '';

  headers: object = [
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      width: '15%',
      sortable: false
    },
    { text: 'Title', value: 'title', align: 'left', width: '20%' },
    {
      text: 'Topics',
      value: 'topics',
      align: 'center',
      sortable: false
    },
    {
      text: 'Submitted Status',
      value: 'submittedStatus',
      align: 'left',
      width: '10%'
    },
    {
      text: 'Justification',
      value: 'justification',
      align: 'left',
      width: '20%'
    },
    {
      text: 'Creation Date',
      value: 'creationDate',
      width: '10%',
      align: 'center'
    }
  ];

  @Watch('evaluateQuestion')
  closeError() {
    if (!this.evaluateQuestion) {
      this.currentStudentQuestion = null;
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

  editStudentQuestion(studentQuestion: StudentQuestion, e?: Event) {
    if (e) e.preventDefault();

    if (!studentQuestion.canEvaluate()) {
      // this.$store.dispatch('error', 'Cannot edit or evaluate this question');
      this.onCloseDialog();
      return;
    }

    this.currentStudentQuestion = studentQuestion;
    this.editAndPromoteStudentQuestionDialog = true;
  }

  showQuestionDialog(studentQuestion: StudentQuestion) {
    this.currentStudentQuestion = studentQuestion;
    this.questionDialog = true;
  }

  async showEvaluateStudentQuestionDialog(sq: StudentQuestion) {
    this.currentStudentQuestion = sq;
    if (!sq.canEvaluate()) {
      this.studentQuestionJustification = true;
    } else {
      this.evaluateQuestion = true;
    }
  }

  async onSaveStudentQuestion(studentQuestion: StudentQuestion) {
    this.studentQuestions = this.studentQuestions.filter(
      sQ => sQ.id !== studentQuestion.id
    );
    this.studentQuestions.unshift(studentQuestion);

    this.onCloseDialog();
  }

  onCloseDialog() {
    this.currentStudentQuestion = null;

    this.evaluateQuestion = false;
    this.questionDialog = false;
    this.editAndPromoteStudentQuestionDialog = false;
    this.studentQuestionJustification = false;
  }

  truncate(s: String): String {
    s.trim();
    const max = 35;
    return s.length > max ? s.substr(0, max - 1) + '...' : s;
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
