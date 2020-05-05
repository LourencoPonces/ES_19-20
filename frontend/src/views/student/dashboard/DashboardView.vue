<template>
  <div>
    <div class="container">
      <h2>Dashboard</h2>
      <div v-if="myStats" class="dashboard-stats-container">
        <div class="items">
          <v-tooltip top>
            <template v-slot:activator="{ on }">
              <v-icon
                v-if="!myStats.isPublic(myStats.statsNames.REQUESTS_SUBMITTED)"
                v-on="on"
                medium
                class="mr-2"
                >fas fa-eye-slash</v-icon
              >
              <v-icon v-else v-on="on" medium class="mr-2">fas fa-eye</v-icon>
            </template>
            <span
              v-if="!myStats.isPublic(myStats.statsNames.REQUESTS_SUBMITTED)"
            >
              Make Public
            </span>
            <span v-else>Make Private</span>
          </v-tooltip>
          <div class="icon-wrapper" ref="requestsSubmitted">
            <animated-number
              :number="
                myStats.getStatValue(myStats.statsNames.REQUESTS_SUBMITTED)
              "
            />
          </div>
          <div class="project-name">
            <p>Clarification Requests Submitted</p>
          </div>
        </div>
        <div class="items">
          <v-tooltip top>
            <template v-slot:activator="{ on }">
              <v-icon
                v-if="!myStats.isPublic(myStats.statsNames.PUBLIC_REQUESTS)"
                v-on="on"
                medium
                class="mr-2"
                >fas fa-eye-slash</v-icon
              >
              <v-icon v-else v-on="on" medium class="mr-2">fas fa-eye</v-icon>
            </template>
            <span v-if="!myStats.isPublic(myStats.statsNames.PUBLIC_REQUESTS)">
              Make Public
            </span>
            <span v-else>Make Private</span>
          </v-tooltip>
          <div class="icon-wrapper" ref="publicRequests">
            <animated-number
              :number="myStats.getStatValue(myStats.statsNames.PUBLIC_REQUESTS)"
            />
          </div>
          <div class="project-name">
            <p>Public Clarification Requests</p>
          </div>
        </div>
        <div class="items">
          <v-tooltip top>
            <template v-slot:activator="{ on }">
              <v-icon
                v-if="!myStats.isPublic(myStats.statsNames.SUBMITTED_QUESTIONS)"
                v-on="on"
                medium
                class="mr-2"
                >fas fa-eye-slash</v-icon
              >
              <v-icon v-else v-on="on" medium class="mr-2">fas fa-eye</v-icon>
            </template>
            <span
              v-if="!myStats.isPublic(myStats.statsNames.SUBMITTED_QUESTIONS)"
            >
              Make Public
            </span>
            <span v-else>Make Private</span>
          </v-tooltip>
          <div class="icon-wrapper" ref="requestsSubmitted">
            <animated-number
              :number="
                myStats.getStatValue(myStats.statsNames.SUBMITTED_QUESTIONS)
              "
            />
          </div>
          <div class="project-name">
            <p>Student Questions Submitted</p>
          </div>
        </div>
        <div class="items">
          <v-tooltip top>
            <template v-slot:activator="{ on }">
              <v-icon
                v-if="!myStats.isPublic(myStats.statsNames.APPROVED_QUESTIONS)"
                v-on="on"
                medium
                class="mr-2"
                >fas fa-eye-slash</v-icon
              >
              <v-icon v-else v-on="on" medium class="mr-2">fas fa-eye</v-icon>
            </template>
            <span
              v-if="!myStats.isPublic(myStats.statsNames.APPROVED_QUESTIONS)"
            >
              Make Public
            </span>
            <span v-else>Make Private</span>
          </v-tooltip>
          <div class="icon-wrapper" ref="requestsSubmitted">
            <animated-number
              :number="
                myStats.getStatValue(myStats.statsNames.APPROVED_QUESTIONS)
              "
            />
          </div>
          <div class="project-name">
            <p>Student Questions Submitted</p>
          </div>
        </div>
      </div>
    </div>
    <v-card class="table">
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
        <template v-slot:item.action="{ item }">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                medium
                class="mr-2"
                v-on="on"
                @click="showDashboardStatsDialog(item)"
                >visibility</v-icon
              >
            </template>
            <span>Show stats</span>
          </v-tooltip>
        </template>
      </v-data-table>
      <show-dashboard-stats-dialog
        v-if="dashboardUserToSee"
        v-model="dashboardStatsDialog"
        :student="dashboardUserToSee"
        :userStats="userStats"
        v-on:close-show-dashboard-stats-dialog="onCloseShowDashboardStatsDialog"
      />
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import DashboardStats from '@/models/statement/DashboardStats';
import RemoteServices from '@/services/RemoteServices';
import Course from '@/models/user/Course';
import AnimatedNumber from '@/components/AnimatedNumber.vue';
import { Student } from '@/models/management/Student';
import ShowDashboardStatsDialog from '@/views/student/dashboard/ShowDashboardStatsDialog.vue';

@Component({
  components: {
    AnimatedNumber,
    'show-dashboard-stats-dialog': ShowDashboardStatsDialog
  }
})
export default class DashboardView extends Vue {
  myStats: DashboardStats | null = null;
  statVisibility = ['PUBLIC', 'PRIVATE'];
  course: Course | null = null;
  students: Student[] = [];
  search: string = '';
  dashboardStatsDialog: boolean = false;
  dashboardUserToSee: Student | null = null;
  userStats: DashboardStats | null = null;
  headers: object = [
    { text: 'Name', value: 'name', align: 'left', width: '40%' },
    {
      text: 'Number',
      value: 'number',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'center',
      width: '10%'
    }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.course = this.$store.getters.getCurrentCourse;
      this.myStats = await RemoteServices.getUserDashboardStats(
        this.$store.getters.getUser.id
      );
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

  async showDashboardStatsDialog(student: Student) {
    this.dashboardUserToSee = student;
    await this.$store.dispatch('loading');
    try {
      this.userStats = await RemoteServices.getUserDashboardStats(student.id);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
    this.dashboardStatsDialog = true;
  }

  onCloseShowDashboardStatsDialog() {
    this.dashboardStatsDialog = false;
    this.dashboardUserToSee = null;
  }
}
</script>

<style lang="scss" scoped>
.dashboard-stats-container {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: stretch;
  align-content: center;
  height: 50%;

  .items {
    background-color: rgba(255, 255, 255, 0.75);
    color: #1976d2;
    border-radius: 5px;
    flex-basis: 25%;
    margin: 20px;
    max-height: 250px;
    cursor: pointer;
    transition: all 0.6s;

    .v-icon {
      left: 55px;
      top: 10px;
    }
  }
}

.icon-wrapper,
.project-name {
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-wrapper {
  font-size: 75px;
  transform: translateY(0px);
  transition: all 0.6s;
}

.icon-wrapper {
  align-self: end;
}

.project-name {
  align-self: start;
}
.project-name p {
  font-size: 20px;
  font-weight: bold;
  letter-spacing: 2px;
  transform: translateY(0px);
  transition: all 0.5s;
}
</style>
