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
      data-cy="studentQuestionTable"
      style="{table-layout : fixed;}"
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
          <v-btn
            color="primary"
            dark
            @click="newStudentQuestion"
            data-cy="NewQuestion"
          >
            New Question
          </v-btn>
        </v-card-title>
      </template>

      <template v-slot:item.title="{ item }">
        <p
          @click="showStudentQuestionDialog(item)"
          @contextmenu="editStudentQuestion(item, $event)"
          style="cursor: pointer"
        >
          {{ item.title }}
        </p>
      </template>

      <template v-slot:item.topics="{ item }">
        <v-chip-group data-cy="questionTopics">
          <v-chip v-for="topic in item.topics" :key="topic.name">
            {{ topic.name }}
          </v-chip>
        </v-chip-group>
      </template>

      <template v-slot:item.submittedStatus="{ item }">
        <v-chip
          :color="item.getEvaluationColor()"
          small
          @click="showJustification(item)"
        >
          <span data-cy="showStatus">{{ item.submittedStatus }}</span>
        </v-chip>
      </template>

      <template v-slot:item.justification="{ item }">
        <p
          @click="showJustification(item)"
          style="cursor:pointer"
          data-cy="show-justification"
        >
          {{ truncate(item.justification) }}
        </p>
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
              large
              class="mr-2"
              v-on="on"
              @click="showStudentQuestionDialog(item)"
              data-cy="showStudentQuestion"
              >visibility</v-icon
            >
          </template>
          <span>Show Question</span>
        </v-tooltip>
        <v-tooltip bottom v-if="item.isChangeable()">
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
          <span>Edit Question</span>
        </v-tooltip>
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              large
              class="mr-2"
              v-on="on"
              @click="duplicateStudentQuestion(item)"
              data-cy="duplicateStudentQuestion"
              >cached</v-icon
            >
          </template>
          <span>Duplicate Question</span>
        </v-tooltip>
        <v-tooltip bottom v-if="item.isChangeable()">
          <template v-slot:activator="{ on }">
            <v-icon
              large
              class="mr-2"
              v-on="on"
              @click="deleteStudentQuestion(item)"
              data-cy="deleteStudentQuestion"
              color="red"
              >delete</v-icon
            >
          </template>
          <span>Delete Question</span>
        </v-tooltip>
        <v-tooltip bottom v-if="item.justification">
          <template v-slot:activator="{ on }">
            <v-icon
              large
              class="mr-2"
              v-on="on"
              @click="showJustification(item)"
              data-cy="checkJustification"
              >question_answer</v-icon
            >
          </template>
          <span>Justification</span>
        </v-tooltip>
      </template>
    </v-data-table>
    <edit-student-question-dialog
      v-if="currentStudentQuestion"
      v-model="editStudentQuestionDialog"
      :studentQuestion="currentStudentQuestion"
      :topics="topics"
      v-on:save-student-question="onSaveStudentQuestion"
    />
    <show-student-question-dialog
      v-if="currentStudentQuestion"
      v-model="studentQuestionDialog"
      :studentQuestion="currentStudentQuestion"
      v-on:close-show-student-question-dialog="onCloseShowStudentQuestionDialog"
    />
    <show-student-question-justification
      v-if="currentStudentQuestion"
      v-model="studentQuestionJustification"
      :studentQuestion="currentStudentQuestion"
      v-on:close-show-student-question-justification="
        onCloseShowStudentQuestionJustification
      "
    />
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import StudentQuestion from '@/models/management/StudentQuestion';
import Image from '@/models/management/Image';
import Topic from '@/models/management/Topic';
import ShowStudentQuestionDialog from '@/views/student/ShowStudentQuestionDialog.vue';
import EditStudentQuestionDialog from '@/views/student/EditStudentQuestionDialog.vue';
import ShowStudentQuestionJustification from '@/views/student/ShowStudentQuestionJustification.vue';

@Component({
  components: {
    'show-student-question-dialog': ShowStudentQuestionDialog,
    'edit-student-question-dialog': EditStudentQuestionDialog,
    'show-student-question-justification': ShowStudentQuestionJustification
  }
})
export default class StudentQuestionView extends Vue {
  studentQuestions: StudentQuestion[] = [];
  topics: Topic[] = [];
  currentStudentQuestion: StudentQuestion | null = null;
  editStudentQuestionDialog: boolean = false;
  studentQuestionDialog: boolean = false;
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
      text: 'Image',
      value: 'image',
      align: 'center',
      width: '5%',
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

  @Watch('editStudentQuestionDialog')
  closeError() {
    if (!this.editStudentQuestionDialog) {
      this.currentStudentQuestion = null;
    }
  }

  newStudentQuestion() {
    this.currentStudentQuestion = new StudentQuestion();
    this.editStudentQuestionDialog = true;
  }

  editStudentQuestion(studentQuestion: StudentQuestion, e?: Event) {
    if (e) e.preventDefault();
    if (!studentQuestion.isChangeable()) {
      this.$store.dispatch('error', 'Cannot edit this question');
      return;
    }
    this.currentStudentQuestion = studentQuestion;
    this.editStudentQuestionDialog = true;
  }

  duplicateStudentQuestion(studentQuestion: StudentQuestion) {
    this.currentStudentQuestion = new StudentQuestion(studentQuestion);
    this.currentStudentQuestion.id = null;
    this.currentStudentQuestion.justification = null;
    this.currentStudentQuestion.submittedStatus = StudentQuestion.getWaitingForApproval();
    this.currentStudentQuestion.options.forEach(option => {
      option.id = null;
    });
    this.editStudentQuestionDialog = true;
  }

  async created() {
    await this.$store.dispatch('loading');
    try {
      [this.topics, this.studentQuestions] = await Promise.all([
        RemoteServices.getTopics(),
        RemoteServices.getStudentQuestionStatuses()
      ]);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  customFilter(
    value: string,
    search: string,
    studentQuestion: StudentQuestion
  ) {
    return (
      search != null &&
      JSON.stringify(studentQuestion)
        .toLowerCase()
        .indexOf(search.toLowerCase()) !== -1
    );
  }

  async onSaveStudentQuestion(studentQuestion: StudentQuestion) {
    this.studentQuestions = this.studentQuestions.filter(
      sQ => sQ.id !== studentQuestion.id
    );
    this.studentQuestions.unshift(studentQuestion);
    this.editStudentQuestionDialog = false;
    this.currentStudentQuestion = null;
  }

  onCloseShowStudentQuestionDialog() {
    this.studentQuestionDialog = false;
  }

  onCloseShowStudentQuestionJustification() {
    this.studentQuestionJustification = false;
  }

  async handleFileUpload(event: File, studentQuestion: StudentQuestion) {
    if (studentQuestion.id) {
      try {
        const imageURL = await RemoteServices.uploadImage(
          event,
          studentQuestion.id
        );
        studentQuestion.image = new Image();
        studentQuestion.image.url = imageURL;
        confirm('Image ' + imageURL + ' was uploaded!');
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  async deleteStudentQuestion(toDeleteStudentquestion: StudentQuestion) {
    if (
      toDeleteStudentquestion.id &&
      confirm('Are you sure you want to delete this question?')
    ) {
      try {
        await RemoteServices.deleteStudentQuestion(toDeleteStudentquestion.id);
        this.studentQuestions = this.studentQuestions.filter(
          question => question.id != toDeleteStudentquestion.id
        );
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  showStudentQuestionDialog(studentQuestion: StudentQuestion) {
    this.currentStudentQuestion = studentQuestion;
    this.studentQuestionDialog = true;
  }

  showJustification(studentQuestion: StudentQuestion) {
    this.currentStudentQuestion = studentQuestion;
    this.studentQuestionJustification = true;
  }

  truncate(s: String): String {
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
