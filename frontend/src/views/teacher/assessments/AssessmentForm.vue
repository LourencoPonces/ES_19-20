<template>
  <v-card v-if="!isMobile" class="table">
    <v-card-title>
      <span>Create Assessment</span>
      <v-spacer />
      <v-btn color="primary" dark @click="$emit('switchMode')">
        {{ editMode ? 'Close' : 'Create' }}
      </v-btn>

      <v-btn color="primary" dark @click="saveAssessment">Save</v-btn>
    </v-card-title>
    <v-card-text>
      <v-container fluid>
        <v-row>
          <v-col>
            <v-text-field
              v-model="assessment.title"
              label="Title"
            ></v-text-field>
          </v-col>

          <v-col>
            <v-text-field
              min="0"
              step="1"
              type="number"
              label="Order"
              :value="assessment.sequence"
              @change="assessment.sequence = Number($event)"
            ></v-text-field>
          </v-col>
        </v-row>
        <v-row>
          <v-col class="light-green lighten-4">
            <v-data-table
              :headers="topicHeaders"
              :custom-filter="topicFilter"
              :items="assessment.topicConjunctions"
              :search="JSON.stringify(currentTopicsSearch)"
              :mobile-breakpoint="0"
              :items-per-page="15"
              :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
            >
              <template v-slot:top>
                <h2>Currently selected</h2>
                <v-autocomplete
                  v-model="currentTopicsSearch"
                  label="Search"
                  :items="allTopics"
                  :filter="topicSearch"
                  :search-input.sync="currentTopicsSearchText"
                  @change="currentTopicsSearchText = ''"
                  item-text="name"
                  return-object
                  chips
                  small-chips
                  clearable
                  deletable-chips
                  multiple
                  dense
                  class="mx-4"
                >
                </v-autocomplete>
              </template>
              <template v-slot:item.topics="{ item }">
                <div v-if="item.topics.length > 0">
                  <v-chip v-for="topic in item.topics" :key="topic.id">
                    {{ topic.name }}
                  </v-chip>
                </div>
                <div v-else>
                  No Topic
                </div>
              </template>
              <template v-slot:item.action="{ item }">
                <v-tooltip bottom>
                  <template v-slot:activator="{ on }">
                    <v-icon
                      medium
                      class="mr-2"
                      v-on="on"
                      @click="removeTopicConjunction(item)"
                    >
                      chevron_right</v-icon
                    >
                  </template>
                  <span>Remove from Assessment</span>
                </v-tooltip>

                <v-tooltip bottom>
                  <template v-slot:activator="{ on }">
                    <v-icon
                      meidium
                      class="mr-2"
                      v-on="on"
                      @click="showQuestionsDialog(item)"
                    >
                      visibility</v-icon
                    >
                  </template>
                  <span>Show Questions</span>
                </v-tooltip>
              </template>
            </v-data-table>
          </v-col>
          <v-col class="red lighten-4">
            <v-data-table
              :headers="topicHeaders"
              :custom-filter="topicFilter"
              :items="topicConjunctions"
              :search="JSON.stringify(allTopicsSearch)"
              :mobile-breakpoint="0"
              :items-per-page="15"
              :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
            >
              <template v-slot:top>
                <h2>Available topics</h2>
                <v-autocomplete
                  v-model="allTopicsSearch"
                  label="Search"
                  :items="allTopics"
                  :filter="topicSearch"
                  :search-input.sync="allTopicsSearchText"
                  @change="allTopicsSearchText = ''"
                  item-text="name"
                  return-object
                  chips
                  small-chips
                  clearable
                  deletable-chips
                  multiple
                  dense
                  class="mx-4"
                >
                </v-autocomplete>
              </template>
              <template v-slot:item.topics="{ item }">
                <div v-if="item.topics.length > 0">
                  <div v-if="item.topics">
                    <v-chip v-for="topic in item.topics" :key="topic.id">
                      {{ topic.name }}
                    </v-chip>
                  </div>
                  <div v-else>
                    No Topic
                  </div>
                </div>
                <div v-else>
                  No Topic
                </div>
              </template>
              <template v-slot:item.action="{ item }">
                <v-tooltip bottom>
                  <template v-slot:activator="{ on }">
                    <v-icon
                      medium
                      class="mr-2"
                      v-on="on"
                      @click="addTopicConjunction(item)"
                    >
                      chevron_left</v-icon
                    >
                  </template>
                  <span>Add to Assessment</span>
                </v-tooltip>
                <v-tooltip bottom>
                  <template v-slot:activator="{ on }">
                    <v-icon
                      medium
                      class="mr-2"
                      v-on="on"
                      @click="showQuestionsDialog(item)"
                    >
                      visibility</v-icon
                    >
                  </template>
                  <span>Show Questions</span>
                </v-tooltip>
              </template>
            </v-data-table>
          </v-col>
        </v-row>
      </v-container>
      <v-btn
        color="primary"
        dark
        v-if="editMode"
        @click="showQuestionsDialog(null)"
        >Show {{ selectedQuestions.length }} selected questions</v-btn
      >
    </v-card-text>

    <v-dialog
      v-model="questionsDialog"
      @keydown.esc="closeQuestionsDialog"
      max-width="75%"
    >
      <v-card v-if="questionsToShow">
        <v-card-text>
          <ol>
            <li
              v-for="question in questionsToShow"
              :key="question.id"
              class="text-left"
            >
              <span
                v-html="convertMarkDown(question.content, question.image)"
              ></span>
              <ul>
                <li v-for="option in question.options" :key="option.number">
                  <span
                    v-html="convertMarkDown(option.content)"
                    v-bind:class="[option.correct ? 'font-weight-bold' : '']"
                  ></span>
                </li>
              </ul>
              <br />
            </li>
          </ol>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn dark color="primary" @click="closeQuestionsDialog"
            >close</v-btn
          >
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-card>
  <edit-assessment-mobile
    v-else
    v-model="dialog"
    :assessment="assessment"
    :topicConjunctions="topicConjunctions"
    v-on:set-status="setStatus"
    v-on:save-assessment="saveAssessment"
    v-on:delete-assessment="deleteAssessment"
    v-on:close-edit-assessment="closeEditDialog"
  />
</template>

<script lang="ts">
import { Component, Vue, Prop, Watch, Model } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Assessment from '@/models/management/Assessment';
import Question from '@/models/management/Question';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import TopicConjunction from '@/models/management/TopicConjunction';
import { _ } from 'vue-underscore';
import Topic from '@/models/management/Topic';
import EditAssessmentMobile from '@/views/teacher/assessments/EditAssessmentMobile.vue';

@Component({
  components: {
    'edit-assessment-mobile': EditAssessmentMobile
  }
})
export default class AssessmentForm extends Vue {
  @Prop({ type: Assessment, required: true }) readonly assessment!: Assessment;
  @Prop({ type: Boolean, required: true }) readonly editMode!: boolean;
  @Prop({ type: Boolean, required: true }) readonly isMobile!: boolean;
  @Prop(Boolean) readonly dialog!: boolean;
  currentTopicsSearch: string = '';
  currentTopicsSearchText: string = '';
  allTopicsSearch: string = '';
  allTopicsSearchText: string = '';

  questionsDialog: boolean = false;
  allTopics: Topic[] = [];
  topicConjunctions: TopicConjunction[] = [];
  questionsToShow: Question[] = [];
  allQuestions: Question[] = [];
  selectedQuestions: Question[] = [];

  topicHeaders: object = [
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      width: '30%',
      sortable: false
    },
    {
      text: 'Topics',
      value: 'topics',
      align: 'left',
      sortable: false
    }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      [this.allQuestions, this.allTopics] = await Promise.all([
        RemoteServices.getQuestions(),
        RemoteServices.getTopics()
      ]);
      this.calculateTopicCombinations();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  // Calculates the ((set of (topics of all the questions)) not present in the current assessment)
  @Watch('assessment')
  calculateTopicCombinations() {
    if (this.editMode) {
      this.topicConjunctions = [];
      this.allQuestions.map((question: Question) => {
        if (
          !this.contains(this.topicConjunctions, question.topics) &&
          !this.contains(this.assessment.topicConjunctions, question.topics)
        ) {
          let topicConjunction = new TopicConjunction();
          topicConjunction.topics = question.topics;
          this.topicConjunctions.push(topicConjunction);
        }
      });
    }
  }

  // Checks if the topics of one topicConjunction has an exact match to the topicArray
  contains(topicConjunctions: TopicConjunction[], topicArray: Topic[]) {
    return (
      topicConjunctions.filter(topicConjunction =>
        _.isEqual(
          topicConjunction.topics.sort((a, b) => (a.name > b.name ? 1 : -1)),
          topicArray.sort((a, b) => (a.name > b.name ? 1 : -1))
        )
      ).length !== 0
    );
  }

  topicFilter(
    value: string,
    search: string,
    topicConjunction: TopicConjunction
  ) {
    let searchTopics = JSON.parse(search);

    if (searchTopics !== '') {
      return searchTopics
        .map((searchTopic: Topic) => searchTopic.name)
        .every((t: string) =>
          topicConjunction.topics.map(topic => topic.name).includes(t)
        );
    }
    return true;
  }

  topicSearch(topic: Topic, search: string) {
    return (
      search != null &&
      topic.name.toLowerCase().indexOf(search.toLowerCase()) !== -1
    );
  }

  async saveAssessment() {
    if (this.assessment && !this.assessment.title) {
      await this.$store.dispatch('error', 'Assessment must have title');
      return;
    }

    try {
      let updatedAssessment: Assessment = await RemoteServices.saveAssessment(
        this.assessment
      );
      this.$emit('updateAssessment', updatedAssessment);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  showQuestionsDialog(topicConjunction: TopicConjunction) {
    if (topicConjunction !== null) {
      this.questionsToShow = this.allQuestions.filter(question => {
        return _.isEqual(topicConjunction.topics, question.topics);
      });
    } else {
      this.questionsToShow = this.allQuestions.filter(question => {
        return (
          this.assessment.topicConjunctions.filter(topicConjunction => {
            return _.isEqual(topicConjunction.topics, question.topics);
          }).length !== 0
        );
      });
    }
    this.questionsDialog = true;
  }

  closeQuestionsDialog() {
    this.questionsDialog = false;
    this.questionsToShow = [];
  }

  removeTopicConjunction(topicConjuntion: TopicConjunction) {
    this.topicConjunctions.push(topicConjuntion);
    this.assessment.topicConjunctions = this.assessment.topicConjunctions.filter(
      tc => tc.sequence != topicConjuntion.sequence
    );
  }

  addTopicConjunction(topicConjuntion: TopicConjunction) {
    this.assessment.topicConjunctions.push(topicConjuntion);
    this.topicConjunctions = this.topicConjunctions.filter(
      tc => tc.sequence !== topicConjuntion.sequence
    );
  }

  @Watch('assessment.topicConjunctions', { deep: true })
  recalculateQuestionList() {
    if (this.assessment) {
      this.selectedQuestions = this.allQuestions.filter(question => {
        return this.assessment.topicConjunctions.find(topicConjunction => {
          return (
            String(question.topics.map(topic => topic.id).sort()) ===
            String(topicConjunction.topics.map(topic => topic.id).sort())
          );
        });
      });
    }
  }

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }

  setStatus(id: number, status: string) {
    this.$emit('set-status', id, status);
  }

  deleteAssessment(id: number) {
    this.$emit('delete-assessment', id);
  }

  closeEditDialog() {
    this.$emit('close-edit-dialog');
  }
}
</script>
