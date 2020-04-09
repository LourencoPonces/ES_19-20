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
      v-on:save-student-question="onSaveStudentQuestion"
    />
    <show-student-question-dialog
      v-if="currentStudentQuestion"
      v-model="studentQuestionDialog"
      :studentQuestion="currentStudentQuestion"
      v-on:close-show-student-question-dialog="onCloseShowStudentQuestionDialog"
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
    { text: 'Submitted Status', value: 'status', align: 'center' },
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

  async onSaveStudentQuestion(studentQuestion: StudentQuestion) {
    this.studentQuestions = this.studentQuestions.filter(
      studentQuestion => studentQuestion.id !== studentQuestion.id
    );
    this.studentQuestions.unshift(studentQuestion);
    this.editStudentQuestionDialog = false;
    this.currentStudentQuestion = null;
    confirm('Student Question successfully submitted');
  }

  onCloseShowStudentQuestionDialog() {
    this.studentQuestionDialog = false;
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
