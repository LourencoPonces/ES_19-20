<template>
  <v-card v-if="editMode && assessment" class="table">
    <v-card-title>
      <span class="headline">Create Assessment</span>
      <v-btn color="primary" dark @click="$emit('switchMode')">
        {{ editMode ? 'Close' : 'Create' }}
      </v-btn>

      <v-btn color="primary" dark @click="saveAssessment">Save</v-btn>
    </v-card-title>
    <v-card-text>
      <v-text-field v-model="assessment.title" label="Title"></v-text-field>
      <v-layout row wrap>
        <v-flex class="text-left">
          <v-data-table
            :headers="topicHeaders"
            :items="assessment.topicConjunctions"
            :items-per-page="15"
            :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
            :search="JSON.stringify(currentTopicsSearch)"
            :custom-filter="topicFilter"
          >
            <template v-slot:top>
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
              <v-chip v-for="topic in item.topics" :key="topic.id">
                {{ topic.name }}
              </v-chip>
            </template>
            <template v-slot:item.action="{ item }">
              <v-tooltip bottom>
                <template v-slot:activator="{ on }">
                  <v-icon
                    small
                    class="mr-2"
                    v-on="on"
                    @click="removeTopicConjunction(item)"
                  >
                    remove</v-icon
                  >
                </template>
                <span>Remove from Assessment</span>
              </v-tooltip>

              <v-tooltip bottom>
                <template v-slot:activator="{ on }">
                  <v-icon
                    small
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
        </v-flex>
        <v-flex class="text-left">
          <v-data-table
            :headers="topicHeaders"
            :items="topicConjunctions"
            :items-per-page="15"
            :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
            :search="JSON.stringify(allTopicsSearch)"
            :custom-filter="topicFilter"
          >
            <template v-slot:top>
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
              <v-chip v-for="topic in item.topics" :key="topic.id">
                {{ topic.name }}
              </v-chip>
            </template>
            <template v-slot:item.action="{ item }">
              <v-tooltip bottom>
                <template v-slot:activator="{ on }">
                  <v-icon
                    small
                    class="mr-2"
                    v-on="on"
                    @click="addTopicConjunction(item)"
                  >
                    add</v-icon
                  >
                </template>
                <span>Add to Assessment</span>
              </v-tooltip>
              <v-tooltip bottom>
                <template v-slot:activator="{ on }">
                  <v-icon
                    small
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
        </v-flex>
      </v-layout>
      <v-btn
        color="primary"
        dark
        v-if="editMode"
        @click="showQuestionsDialog(null)"
        >Show {{ selectedQuestions.length }} selected questions</v-btn
      >
    </v-card-text>

    <v-dialog
      v-model="showQuestions"
      @keydown.esc="closeQuestionsDialog"
      max-width="75%"
    >
      <v-card v-if="questionsToShow">
        <v-card-text>
          <v-container grid-list-md fluid>
            <v-layout column wrap>
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
                        v-html="convertMarkDown(option.content, null)"
                        v-bind:class="[
                          option.correct ? 'font-weight-bold' : ''
                        ]"
                      ></span>
                    </li>
                  </ul>
                  <br />
                </li>
              </ol>
            </v-layout>
          </v-container>
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
</template>

<script lang="ts">
import { Component, Vue, Prop, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Assessment from '@/models/management/Assessment';
import Question from '@/models/management/Question';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import TopicConjunction from '@/models/management/TopicConjunction';
import { _ } from 'vue-underscore';
import Topic from '@/models/management/Topic';

@Component
export default class AssessmentForm extends Vue {
  @Prop(Assessment) readonly assessment!: Assessment;
  @Prop(Boolean) readonly editMode!: boolean;
  currentTopicsSearch: string = '';
  currentTopicsSearchText: string = '';
  allTopicsSearch: string = '';
  allTopicsSearchText: string = '';

  showQuestions: boolean = false;
  allTopics: Topic[] = [];
  topicConjunctions: TopicConjunction[] = [];
  questionsToShow: Question[] = [];
  allQuestions: Question[] = [];
  selectedQuestions: Question[] = [];

  topicHeaders: object = [
    {
      text: 'Topics',
      value: 'topics',
      align: 'left',
      width: '99%',
      sortable: false
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'center',
      width: '1%',
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
    this.showQuestions = true;
  }

  closeQuestionsDialog() {
    this.showQuestions = false;
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
}
</script>

<style lang="scss" scoped></style>