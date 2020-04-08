<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :custom-filter="customFilter"
      :items="questions"
      :search="search"
      multi-sort
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

          <v-spacer />
          <v-btn color="primary" dark @click="newStudentQuestion">
            New Question
          </v-btn>
        </v-card-title>
      </template>

      <template v-slot:item.content="{ item }">
        <p
          v-html="convertMarkDownNoFigure(item.content, null)"
          @click="showQuestionDialog(item)"
      /></template>

      <template v-slot:item.topics="{ item }">
        <edit-question-topics
          :question="item"
          :topics="topics"
          v-on:question-changed-topics="onQuestionChangedTopics"
        />
      </template>

      <template v-slot:item.difficulty="{ item }">
        <v-chip
          v-if="item.difficulty"
          :color="getDifficultyColor(item.difficulty)"
          dark
          >{{ item.difficulty + '%' }}</v-chip
        >
      </template>

      <template v-slot:item.status="{ item }">
        <v-select
          v-model="item.status"
          :items="statusList"
          dense
          @change="setStatus(item.id, item.status)"
        >
          <template v-slot:selection="{ item }">
            <v-chip :color="getStatusColor(item)" small>
              <span>{{ item }}</span>
            </v-chip>
          </template>
        </v-select>
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
        <v-tooltip bottom v-if="item.numberOfAnswers === 0">
          <template v-slot:activator="{ on }">
            <v-icon small class="mr-2" v-on="on" @click="editQuestion(item)"
              >edit</v-icon
            >
          </template>
          <span>Edit Question</span>
        </v-tooltip>
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="duplicateQuestion(item)"
              >cached</v-icon
            >
          </template>
          <span>Duplicate Question</span>
        </v-tooltip>
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="deleteQuestion(item)"
              color="red"
              >delete</v-icon
            >
          </template>
          <span>Delete Question</span>
        </v-tooltip>
      </template>
    </v-data-table>
    <edit-student-question-dialog
      v-if="currentStudentQuestion"
      v-model="editStudentQuestionDialog"
      :studentQuestion="currentStudentQuestion"
      :topics="topics"
      v-on:save-question="onSaveQuestion"
    />
    <show-student-question-dialog
      v-if="currentStudentQuestion"
      v-model="studentQuestionDialog"
      :studentQuestion="currentStudentQuestion"
      v-on:close-show-student-question-dialog="onCloseShowQuestionDialog"
    />
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { convertMarkDownNoFigure } from '@/services/ConvertMarkdownService';
import StudentQuestion from '@/models/management/StudentQuestion';
import Image from '@/models/management/Image';
import Topic from '@/models/management/Topic';
import ShowStudentQuestionDialog from '@/views/student/ShowStudentQuestionDialog.vue';
import EditStudentQuestionDialog from '@/views/student/EditStudentQuestionDialog.vue';

@Component({
  components: {
    'show-student-question-dialog': ShowStudentQuestionDialog,
    'edit-student-question-dialog': EditStudentQuestionDialog
  }
})
/*export default class StudentQuestionView extends Vue {
  questions: Question[] = [];
  topics: Topic[] = [];
  currentStudentQuestion: Question | null = null;
  editStudentQuestionDialog: boolean = false;
  questionDialog: boolean = false;
  search: string = '';
  statusList = ['DISABLED', 'AVAILABLE', 'REMOVED'];

  headers: object = [
    { text: 'Title', value: 'title', align: 'center' },
    { text: 'Question', value: 'content', align: 'left' },
    {
      text: 'Topics',
      value: 'topics',
      align: 'center',
      sortable: false
    },
    { text: 'Difficulty', value: 'difficulty', align: 'center' },
    { text: 'Answers', value: 'numberOfAnswers', align: 'center' },
    {
      text: 'Nº of generated quizzes',
      value: 'numberOfGeneratedQuizzes',
      align: 'center'
    },
    {
      text: 'Nº of non generated quizzes',
      value: 'numberOfNonGeneratedQuizzes',
      align: 'center'
    },
    { text: 'Status', value: 'status', align: 'center' },
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

  @Watch('editStudentQuestionDialog')
  closeError() {
    if (!this.editStudentQuestionDialog) {
      this.currentStudentQuestion = null;
    }
  }

  async created() {
    await this.$store.dispatch('loading');
    try {
      [this.topics, this.questions] = await Promise.all([
        RemoteServices.getTopics(),
        RemoteServices.getQuestions()
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

  convertMarkDownNoFigure(text: string, image: Image | null = null): string {
    return convertMarkDownNoFigure(text, image);
  }

  onQuestionChangedTopics(questionId: Number, changedTopics: Topic[]) {
    let question = this.questions.find(
      (question: Question) => question.id == questionId
    );
    if (question) {
      question.topics = changedTopics;
    }
  }

  getDifficultyColor(difficulty: number) {
    if (difficulty < 25) return 'green';
    else if (difficulty < 50) return 'lime';
    else if (difficulty < 75) return 'orange';
    else return 'red';
  }

  async setStatus(questionId: number, status: string) {
    try {
      await RemoteServices.setQuestionStatus(questionId, status);
      let question = this.questions.find(
        question => question.id === questionId
      );
      if (question) {
        question.status = status;
      }
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  getStatusColor(status: string) {
    if (status === 'REMOVED') return 'red';
    else if (status === 'DISABLED') return 'orange';
    else return 'green';
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

  showQuestionDialog(question: Question) {
    this.currentStudentQuestion = question;
    this.questionDialog = true;
  }

  onCloseShowQuestionDialog() {
    this.questionDialog = false;
  }

  newQuestion() {
    this.currentStudentQuestion = new Question();
    this.editStudentQuestionDialog = true;
  }

  editQuestion(question: Question) {
    this.currentStudentQuestion = question;
    this.editStudentQuestionDialog = true;
  }

  duplicateQuestion(question: Question) {
    this.currentStudentQuestion = new Question(question);
    this.currentStudentQuestion.id = null;
    this.editStudentQuestionDialog = true;
  }

  async onSaveQuestion(question: Question) {
    this.questions = this.questions.filter(q => q.id !== question.id);
    this.questions.unshift(question);
    this.editStudentQuestionDialog = false;
    this.currentStudentQuestion = null;
  }

  async exportCourseQuestions() {
    let fileName = this.$store.getters.getCurrentCourse.name + '-Questions.zip';
    try {
      let result = await RemoteServices.exportCourseQuestions();
      const url = window.URL.createObjectURL(result);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  async deleteQuestion(toDeletequestion: Question) {
    if (
      toDeletequestion.id &&
      confirm('Are you sure you want to delete this question?')
    ) {
      try {
        await RemoteServices.deleteQuestion(toDeletequestion.id);
        this.questions = this.questions.filter(
          question => question.id != toDeletequestion.id
        );
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }
}*/
export default class StudentQuestionView extends Vue {
  studentQuestions: StudentQuestion[] = [];
  topics: Topic[] = [];
  currentStudentQuestion: StudentQuestion | null = null;
  editStudentQuestionDialog: boolean = false;
  studentQuestionDialog: boolean = false;
  search: string = '';

  submittedStatusList = ['WAITING_FOR_APPROVAL', 'ACCEPTED', 'REJECTED'];

  headers: object = [
    { text: 'Title', value: 'title', align: 'center' },
    { text: 'Question', value: 'content', align: 'left' },
    {
      text: 'Topics',
      value: 'topics',
      align: 'center',
      sortable: false
    },
    { text: 'Difficulty', value: 'difficulty', align: 'center' },
    { text: 'Answers', value: 'numberOfAnswers', align: 'center' },
    {
      text: 'Nº of generated quizzes',
      value: 'numberOfGeneratedQuizzes',
      align: 'center'
    },
    {
      text: 'Nº of non generated quizzes',
      value: 'numberOfNonGeneratedQuizzes',
      align: 'center'
    },
    { text: 'Status', value: 'status', align: 'center' },
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

  newStudentQuestion() {
    this.currentStudentQuestion = new StudentQuestion();
    this.editStudentQuestionDialog = true;
  }

  async created() {
    await this.$store.dispatch('loading');
    try {
      [this.topics] = await Promise.all([RemoteServices.getTopics()]);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async onSaveQuestion(studentQuestion: StudentQuestion) {
    this.studentQuestions = this.studentQuestions.filter(
      studentQuestion => studentQuestion.id !== studentQuestion.id
    );
    this.studentQuestions.unshift(studentQuestion);
    this.editStudentQuestionDialog = false;
    this.currentStudentQuestion = null;
  }

  onCloseShowQuestionDialog() {
    this.studentQuestionDialog = false;
  }
}
</script>

<style lang="scss" scoped>
// .question-textarea {
//   text-align: left;

//   .CodeMirror,
//   .CodeMirror-scroll {
//     min-height: 200px !important;
//   }
// }
// .option-textarea {
//   text-align: left;

//   .CodeMirror,
//   .CodeMirror-scroll {
//     min-height: 100px !important;
//   }
// }
</style>
