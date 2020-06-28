<template>
  <div class="container">
    <h2>Assessments</h2>
    <!-- BROWSER -->
    <v-card v-if="!editMode && !isMobile" class="table">
      <v-data-table
        :headers="headers"
        :items="assessments"
        :search="search"
        :sort-by="['sequence']"
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
              class="mx-4"
            />

            <v-spacer />
            <v-btn color="primary" dark @click="newAssessment()"
              >New Assessment</v-btn
            >
          </v-card-title>
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
        <template v-slot:item.action="{ item }">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                medium
                class="mr-2"
                v-on="on"
                @click="editAssessment(item.id)"
                >edit</v-icon
              >
            </template>
            <span>Edit Assessment</span>
          </v-tooltip>
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                medium
                class="mr-2"
                v-on="on"
                @click="deleteAssessment(item.id)"
                color="red"
                >delete</v-icon
              >
            </template>
            <span>Delete Assessment</span>
          </v-tooltip>
        </template>
        <template v-slot:item.title="{ item }">
          <p
            @contextmenu="editAssessment(item.id, $event)"
            style="cursor: pointer"
          >
            {{ item.title }}
          </p>
        </template>
      </v-data-table>
      <footer>
        <v-icon class="mr-2">mouse</v-icon>Right-click on assessment's title to
        edit it.
      </footer>

      <v-dialog
        v-model="dialog"
        @keydown.esc="closeAssessment"
        fullscreen
        hide-overlay
        max-width="1000px"
      >
        <v-card v-if="assessment">
          <v-toolbar dark color="primary">
            <v-toolbar-title>{{ assessment.title }}</v-toolbar-title>
            <div class="flex-grow-1"></div>
            <v-toolbar-items>
              <v-btn dark color="primary" @click="closeAssessment">Close</v-btn>
            </v-toolbar-items>
          </v-toolbar>

          <v-card-text>
            <ol>
              <li
                v-for="question in assessment.questions"
                :key="question.sequence"
                class="text-left"
              >
                <span
                  v-html="convertMarkDown(question.content, question.image)"
                />
                <ul>
                  <li v-for="option in question.options" :key="option.number">
                    <span
                      v-html="convertMarkDown(option.content)"
                      v-bind:class="[option.correct ? 'font-weight-bold' : '']"
                    />
                  </li>
                </ul>
                <br />
              </li>
            </ol>
          </v-card-text>

          <v-card-actions>
            <v-spacer />
            <v-btn dark color="primary" @click="closeAssessment">close</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-card>

    <!-- MOBILE -->
    <v-card v-else-if="isMobile" class="table">
      <v-data-table
        :headers="headers_mobile"
        :items="assessments"
        :search="search"
        :sort-by="['sequence']"
        :mobile-breakpoint="0"
        :items-per-page="15"
        :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
      >
        <template v-slot:top>
          <v-card-title>
            <v-row>
              <v-col cols="9">
                <v-text-field
                  v-model="search"
                  append-icon="search"
                  label="Search"
                  class="mx-4"
                />
              </v-col>
              <v-col cols="3" align-self="center">
                <v-btn fab primary small color="primary">
                  <v-icon small class="mr-2" @click="newAssessment()"
                    >fa fa-plus</v-icon
                  >
                </v-btn>
              </v-col>
            </v-row>
          </v-card-title>
        </template>
        <template v-slot:item.title="{ item }">
          <v-row @click="editAssessment(item.id)">
            <v-col align-self="center">
              <v-badge bordered :color="getStatusColor(item.status)" />
            </v-col>
            <v-col cols="10">
              <p>{{ item.title }}</p>
            </v-col>
          </v-row>
        </template>
      </v-data-table>
    </v-card>
    <assessment-form
      v-if="editMode && assessment"
      @switchMode="changeMode"
      @updateAssessment="updateAssessment"
      :assessment="assessment"
      :dialog="dialog"
      :isMobile="isMobile"
      :editMode="editMode"
      v-on:set-status="setStatus"
      v-on:delete-assessment="deleteAssessment"
      v-on:close-edit-dialog="closeAssessment"
    />
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import Assessment from '@/models/management/Assessment';
import AssessmentForm from '@/views/teacher/assessments/AssessmentForm.vue';

@Component({
  components: {
    'assessment-form': AssessmentForm
  }
})
export default class AssessmentsView extends Vue {
  assessments: Assessment[] = [];
  assessment: Assessment | null = null;
  editMode: boolean = false;
  isMobile: boolean = false;
  search: string = '';
  dialog: boolean = false;
  statusList = ['DISABLED', 'AVAILABLE', 'REMOVED'];
  headers: object = [
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      sortable: false,
      width: '15%'
    },
    { text: 'Order', value: 'sequence', align: 'center', width: '95px' },
    { text: 'Title', value: 'title', align: 'left' },
    {
      text: 'Number of questions',
      value: 'numberOfQuestions',
      align: 'center',
      width: '7%'
    },
    { text: 'Status', value: 'status', align: 'center', width: '7%' }
  ];
  headers_mobile: object = [
    { text: 'Title', value: 'title', align: 'center', sortable: false }
  ];

  closeAssessment() {
    this.dialog = false;
    this.changeMode();
  }

  async setStatus(assessmentId: number, status: string) {
    try {
      await RemoteServices.setAssessmentStatus(assessmentId, status);
      let assessment = this.assessments.find(
        assessment => assessment.id === assessmentId
      );
      if (assessment) {
        assessment.status = status;
      }
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  async editAssessment(assessmentId: number, e?: Event) {
    if (e) e.preventDefault();
    try {
      this.assessment = {
        ...this.assessments.find(assessment => assessment.id === assessmentId)!
      };

      this.dialog = this.isMobile;
      this.editMode = true;
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  async deleteAssessment(assessmentId: number) {
    if (confirm('Are you sure you want to delete this assessment?')) {
      try {
        await RemoteServices.deleteAssessment(assessmentId);
        this.assessments = this.assessments.filter(
          assessment => assessment.id !== assessmentId
        );
        if (this.isMobile) {
          this.dialog = false;
          this.changeMode();
        }
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  getStatusColor(status: string) {
    if (status === 'REMOVED') return 'red';
    else if (status === 'DISABLED') return 'orange';
    else return 'green';
  }

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }

  async created() {
    await this.$store.dispatch('loading');
    this.isMobile = window.innerWidth <= 500;
    try {
      this.assessments = await RemoteServices.getAssessments();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  changeMode() {
    this.editMode = !this.editMode;
    if (this.editMode) {
      this.assessment = new Assessment();
    } else {
      this.assessment = null;
    }
  }

  updateAssessment(updatedAssessment: Assessment) {
    this.assessments = this.assessments.filter(
      assessment => assessment.id !== updatedAssessment.id
    );
    this.assessments.unshift(updatedAssessment);
    this.editMode = false;
    this.assessment = null;
  }

  newAssessment() {
    this.assessment = new Assessment();
    this.editMode = true;
    this.dialog = this.isMobile;
  }
}
</script>

<style lang="scss" scoped>
.container {
  max-width: 90%;
  margin-left: auto;
  margin-right: auto;
  padding-left: 10px;
  padding-right: 10px;

  h2 {
    font-size: 26px;
    margin: 20px 0;
    text-align: center;
    small {
      font-size: 0.5em;
    }
  }
}
</style>
