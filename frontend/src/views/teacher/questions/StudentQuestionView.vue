<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :custom-filter="customFilter"
      :items="studentQuestions"
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
        </v-card-title>
      </template>

      <template v-slot:item.content="{ item }">
        <p
          v-html="convertMarkDownNoFigure(item.content, null)"
          @click="showQuestionDialog(item)"
      /></template>

      <template v-slot:item.topics="{ item }">
        <v-chip-group>
          <v-chip v-for="topic in item.topics" :key="topic.name">
            {{ topic.name }}
          </v-chip>
        </v-chip-group>
      </template>

      <template v-slot:item.status="{ item }">
        <v-select
          v-model="item.submittedStatus"
          :items="statusList"
          dense
          @change="evaluate(item, item.submittedStatus)"
        >
          <template v-slot:selection="{ item }">
            <v-chip :color="getEvaluationColor(item)" small>
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
      v-model="editQuestionDialog"
      :studentQuestion="currentQuestion"
      v-on:close-evaluate-question-dialog="onCloseEditStudentQuestionDialog"
    />
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { convertMarkDownNoFigure } from '@/services/ConvertMarkdownService';
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
  studentQuestions: Question[] = [];
  topics: Topic[] = [];
  currentQuestion: StudentQuestion | null = null;
  editQuestionDialog: boolean = false;
  evaluateQuestion: boolean = false;
  questionDialog: boolean = false;
  search: string = '';
  statusList = ['Rejected', 'Approved', 'Waiting for Approval'];

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

  @Watch('editQuestionDialog')
  closeError() {
    if (!this.editQuestionDialog) {
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

  convertMarkDownNoFigure(text: string, image: Image | null = null): string {
    return convertMarkDownNoFigure(text, image);
  }

  async evaluate(question: StudentQuestion, status: string) {
    this.currentQuestion = question;
    this.evaluateQuestion = true;
    return;
    // try {
    //   await RemoteServices.setQuestionStatus(questionId, status);
    //   let question = this.studentQuestions.find(
    //     question => question.id === questionId
    //   );
    //   if (question) {
    //     question.status = status;
    //   }
    // } catch (error) {
    //   await this.$store.dispatch('error', error);
    // }
  }

  getEvaluationColor(status: string) {
    if (status === 'Rejected') return 'red';
    else if (status === 'Waiting for Approval') return 'orange';
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

  showQuestionDialog(question: StudentQuestion) {
    this.currentQuestion = question;
    this.questionDialog = true;
  }

  onCloseShowQuestionDialog() {
    this.questionDialog = false;
  }

  onCloseEditStudentQuestionDialog() {
    this.evaluateQuestion = false;
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
