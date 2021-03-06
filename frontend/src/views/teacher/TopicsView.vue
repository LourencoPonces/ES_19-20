<template v-if="topics">
  <div class="container">
    <h2>Topics</h2>

    <!-- WEB BROWSER -->
    <v-card v-if="!isMobile" class="table">
      <v-data-table
        :headers="headers"
        :custom-filter="customFilter"
        :items="topics"
        :search="search"
        :mobile-breakpoint="0"
        :items-per-page="50"
        :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
      >
        <template v-slot:top>
          <v-card-title>
            <v-text-field
              v-model="search"
              append-icon="search"
              label="Search"
              single-line
              hide-details
            />
            <v-spacer />
            <v-btn color="primary" dark @click="newTopic">New Topic</v-btn>
          </v-card-title>
        </template>
        <template v-slot:item.action="{ item }">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon medium class="mr-2" v-on="on" @click="editTopic(item)"
                >edit</v-icon
              >
            </template>
            <span>Edit Topic</span>
          </v-tooltip>
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                medium
                class="mr-2"
                v-on="on"
                @click="deleteTopic(item)"
                color="red"
                >delete</v-icon
              >
            </template>
            <span>Delete Topic</span>
          </v-tooltip>
        </template>
        <template v-slot:item.name="{ item }">
          <p @contextmenu="editTopic(item, $event)" style="cursor: pointer">
            {{ item.name }}
          </p>
        </template>
      </v-data-table>
      <footer>
        <v-icon class="mr-2">mouse</v-icon>Right-click on topic's name to edit
        it.
      </footer>

      <v-dialog v-model="topicDialog" max-width="75%">
        <v-card>
          <v-card-title>
            <span class="headline">{{ formTitle() }}</span>
          </v-card-title>

          <v-card-text v-if="editedTopic">
            <v-text-field v-model="editedTopic.name" label="Topic" />
          </v-card-text>

          <v-card-actions>
            <v-spacer />
            <v-btn color="error" @click="closeDialogue">Cancel</v-btn>
            <v-btn color="primary" dark @click="saveTopic">Save</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-card>

    <!-- MOBILE -->
    <v-card v-else class="table">
      <v-data-table
        :headers="headers_mobile"
        :custom-filter="customFilter"
        :items="topics"
        :search="search"
        :mobile-breakpoint="0"
        :items-per-page="50"
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
                  single-line
                  hide-details
                />
              </v-col>
              <v-col cols="3" align-self="center">
                <template>
                  <v-btn fab primary small color="primary">
                    <v-icon small class="mr-2" @click="newTopic"
                      >fa fa-plus</v-icon
                    >
                  </v-btn>
                </template>
              </v-col>
            </v-row>
          </v-card-title>
        </template>
        <template v-slot:item.name="{ item }">
          <p @click="editTopic(item, $event)" style="cursor: pointer">
            {{ item.name }}
          </p>
        </template>
      </v-data-table>
      <footer />

      <v-dialog v-model="topicDialog" max-width="75%">
        <v-card>
          <v-card-title>
            <span class="headline">Topic</span>
          </v-card-title>

          <v-card-text>
            <v-text-field v-model="editedTopic.name" label="Name" />
            <b
              >There {{ editedTopic.numberOfQuestions === 1 ? 'is' : 'are' }}
              {{ editedTopic.numberOfQuestions }} question{{
                editedTopic.numberOfQuestions === 1 ? '' : 's'
              }}
              about this topic
            </b>
          </v-card-text>
          <v-card-actions>
            <v-spacer />
            <v-btn
              v-if="editedTopic.numberOfQuestions == 0"
              fab
              color="error"
              small
              @click="deleteTopic(editedTopic)"
            >
              <v-icon medium class="mr-2">
                delete
              </v-icon>
            </v-btn>
            <v-btn fab color="primary" small @click="saveTopic">
              <v-icon medium class="mr-2">
                far fa-save
              </v-icon>
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Topic from '@/models/management/Topic';

@Component
export default class TopicsView extends Vue {
  isMobile: boolean = false;
  topics: Topic[] = [];
  editedTopic: Topic = new Topic();
  topicDialog: boolean = false;
  search: string = '';
  headers: object = [
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      width: '15%',
      sortable: false
    },
    { text: 'Name', value: 'name', align: 'left' },
    {
      text: 'Questions',
      value: 'numberOfQuestions',
      align: 'center',
      width: '115px'
    }
  ];
  headers_mobile: object = [
    { text: 'Topic', value: 'name', align: 'center', sortable: false }
  ];

  async created() {
    await this.$store.dispatch('loading');
    this.isMobile = window.innerWidth <= 500;
    try {
      this.topics = await RemoteServices.getTopics();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  customFilter(value: string, search: string) {
    // noinspection SuspiciousTypeOfGuard,SuspiciousTypeOfGuard
    return (
      search != null &&
      typeof value === 'string' &&
      value.toLocaleLowerCase().indexOf(search.toLocaleLowerCase()) !== -1
    );
  }

  formTitle() {
    return this.editedTopic === null ? 'New Topic' : 'Edit Topic';
  }

  newTopic() {
    this.editedTopic = new Topic();
    this.topicDialog = true;
  }

  closeDialogue() {
    this.topicDialog = false;
  }

  editTopic(topic: Topic, e?: Event) {
    if (e) e.preventDefault();
    this.editedTopic = { ...topic };
    this.topicDialog = true;
  }

  async deleteTopic(toDeleteTopic: Topic) {
    if (confirm('Are you sure you want to delete this topic?')) {
      try {
        await RemoteServices.deleteTopic(toDeleteTopic);
        this.topics = this.topics.filter(
          topic => topic.id !== toDeleteTopic.id
        );
        this.topicDialog = false;
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  async saveTopic() {
    try {
      if (this.editedTopic.id) {
        this.editedTopic = await RemoteServices.updateTopic(this.editedTopic);
        this.topics = this.topics.filter(
          topic => topic.id !== this.editedTopic.id
        );
      } else if (this.editedTopic) {
        this.editedTopic = await RemoteServices.createTopic(this.editedTopic);
      }

      this.topics.unshift(this.editedTopic);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    this.closeDialogue();
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
