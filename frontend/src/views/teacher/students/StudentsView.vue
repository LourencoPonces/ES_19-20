/*<template>
  <div class="container">
    <h2>Students</h2>

    <!-- WEB BROWSER -->
    <v-card class="table" v-if="!isMobile">
      <v-data-table
        :headers="headers"
        :items="students"
        :search="search"
        disable-pagination
        :hide-default-footer="true"
        :mobile-breakpoint="0"
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
          </v-card-title>
        </template>

        <template v-slot:item.percentageOfCorrectAnswers="{ item }">
          <v-chip
            :color="getPercentageColor(item.percentageOfCorrectAnswers)"
            dark
            >{{ item.percentageOfCorrectAnswers + '%' }}</v-chip
          >
        </template>

        <template v-slot:item.percentageOfCorrectTeacherAnswers="{ item }">
          <v-chip
            :color="getPercentageColor(item.percentageOfCorrectTeacherAnswers)"
            dark
            >{{ item.percentageOfCorrectTeacherAnswers + '%' }}</v-chip
          >
        </template>
      </v-data-table>
    </v-card>

    <!-- MOBILE -->
    <v-card class="table" v-else>
      <v-data-table
        :headers="headers_mobile"
        :items="students"
        :search="search"
        disable-pagination
        :hide-default-footer="true"
        :mobile-breakpoint="0"
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
          </v-card-title>
        </template>
        <template v-slot:item.name="{ item }">
          <v-expansion-panels flat>
            <v-expansion-panel>
              <v-expansion-panel-header>
                <p>{{ item.name }}</p>
              </v-expansion-panel-header>
              <v-expansion-panel-content>
                <v-row>
                  <v-col cols="8">
                    <span>Teacher Quizzes:</span>
                  </v-col>
                  <v-col>
                    <span> {{ item.numberOfTeacherQuizzes }}</span>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="8">
                    <span>Generated Quizzes:</span>
                  </v-col>
                  <v-col>
                    <span> {{ item.numberOfStudentQuizzes }}</span>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="8">
                    <span>Total Answers:</span>
                  </v-col>
                  <v-col>
                    <span>{{ item.numberOfAnswers }}</span>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="8">
                    <span>Correct Answers:</span>
                  </v-col>
                  <v-col>
                    <v-chip
                      :color="getPercentageColor(item.percentageOfCorrectAnswers)"
                      dark
                      >{{ item.percentageOfCorrectAnswers }}%</v-chip
                    >
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="8">
                    <span>Answers Teacher Quiz</span>
                  </v-col>
                  <v-col>
                    <span>{{ item.numberOfTeacherAnswers }}</span>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="8">
                    <span>Correct Answers Teacher Quiz</span>
                  </v-col>
                  <v-col>
                    <v-chip
                      :color="getPercentageColor(item.percentageOfCorrectAnswers)"
                      dark
                      >{{ item.percentageOfCorrectTeacherAnswers }}%</v-chip
                    >
                  </v-col>
                </v-row>
              </v-expansion-panel-content>
            </v-expansion-panel>
          </v-expansion-panels>
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Course from '@/models/user/Course';
import { Student } from '@/models/management/Student';

@Component
export default class StudentsView extends Vue {
  isMobile: boolean = false;
  course: Course | null = null;
  students: Student[] = [];
  search: string = '';
  headers: object = [
    { text: 'Name', value: 'name', align: 'left', width: '40%' },
    {
      text: 'Teacher Quizzes',
      value: 'numberOfTeacherQuizzes',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Generated Quizzes',
      value: 'numberOfStudentQuizzes',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Total Answers',
      value: 'numberOfAnswers',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Correct Answers',
      value: 'percentageOfCorrectAnswers',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Answers Teacher Quiz',
      value: 'numberOfTeacherAnswers',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Correct Answers Teacher Quiz',
      value: 'percentageOfCorrectTeacherAnswers',
      align: 'center',
      width: '10%'
    }
  ];

  headers_mobile: object = [
    { text: 'Name', value: 'name', align: 'center', width: '40%', sortable: false }
  ];

  async created() {
    await this.$store.dispatch('loading');
    this.isMobile = window.innerWidth <= 500;
    try {
      this.course = this.$store.getters.getCurrentCourse;
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  @Watch('course')
  async onAcademicTermChange() {
    await this.$store.dispatch('loading');
    try {
      if (this.course) {
        this.students = await RemoteServices.getCourseStudents(this.course);
      }
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  getPercentageColor(percentage: number) {
    if (percentage < 25) return 'red';
    else if (percentage < 50) return 'orange';
    else if (percentage < 75) return 'lime';
    else return 'green';
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
