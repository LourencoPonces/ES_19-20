<template>
  <div class="container">
    <h2>Clarification Requests</h2>
    <v-card>
      <v-data-table
        :headers="headers"
        :items="requests"
        :search="search"
        multi-sort
        show-expand
        single-expand
        :mobile-breakpoint="0"
        :items-per-page="15"
        :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
        class="table"
        data-cy="table"
      >
        <template v-slot:top>
          <v-card-title>
            <v-text-field
              v-model="search"
              append-icon="search"
              label="Search"
              class="mx-2"
              single-line
              hide-details
            />

            <v-spacer />
          </v-card-title>
        </template>

        <template v-slot:item.status="{ item }">
          <v-icon
            v-if="item.isPrivate()"
            small
            class="mr-2"
            :data-cy="'private-' + item.content.slice(0, 15)"
            >fas fa-eye-slash</v-icon
          >
          <v-icon
            v-else
            small
            class="mr-2"
            :data-cy="'public-' + item.content.slice(0, 15)"
            >fas fa-eye</v-icon
          >
        </template>

        <template v-slot:item.content="{ item }">
          <div class="short-content">{{ item.content }}</div>
        </template>

        <template v-slot:item.resolved="{ item }">
          <v-simple-checkbox
            :value="item.resolved"
            readonly
            :aria-label="
              item.resolved
                ? 'request was resolved'
                : 'request was not resolved'
            "
          />
        </template>

        <template v-slot:item.action="{ item }">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                :disabled="item.hasMessages"
                small
                class="mr-2"
                v-on="on"
                @click="deleteRequest(item)"
                color="red"
                data-cy="delete"
                >delete</v-icon
              >
            </template>
            <span>Delete Request</span>
          </v-tooltip>
        </template>

        <template v-slot:expanded-item="{ headers, item }">
          <td :colspan="headers.length" class="clarification-expand-container">
            <h2>Clarification Request:</h2>
            <div class="multiline msg-content">{{ item.content }}</div>

            <h3>Messages:</h3>
            <clarification-thread :request="item"></clarification-thread>
          </td>
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import ClarificationRequest from '@/models/clarification/ClarificationRequest';
import Question from '@/models/management/Question';
import ClarificationThread from '../ClarificationThread.vue';

@Component({
  components: {
    'clarification-thread': ClarificationThread
  }
})
export default class ClarificationsView extends Vue {
  requests: ClarificationRequest[] = [];
  search: string = '';
  newContent: string = '';
  editingItem: ClarificationRequest | null = null;
  dialog: boolean = false;

  headers: object = [
    {
      text: 'Request',
      value: 'content',
      align: 'left'
    },
    {
      text: 'Resolved',
      value: 'resolved',
      align: 'center',
      sortable: false,
      width: '100px'
    },
    {
      text: 'Visibility',
      value: 'status',
      align: 'center',
      sortable: false,
      width: '100px'
    },
    {
      text: 'Creation Date',
      value: 'creationDate',
      align: 'center',
      width: '150px'
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'center',
      sortable: false,
      width: '100px'
    }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.requests = await RemoteServices.getUserClarificationRequests();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async deleteRequest(req: ClarificationRequest) {
    if (
      !confirm('Are you sure you want to delete this clarification request?')
    ) {
      return;
    }
    await this.$store.dispatch('loading');
    try {
      await RemoteServices.deleteClarificationRequest(req.id);
      this.requests.splice(this.requests.indexOf(req), 1);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
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

.clarification-expand-container {
  text-align: left;
  padding: 20px;

  h2,
  h3 {
    margin: 10px 0;
    font-size: initial;
    text-align: left;
  }

  h2 {
    font-size: 20px;
  }

  .msg-content {
    margin-bottom: 10px;
  }
}

.multiline {
  white-space: pre;
}

.short-content {
  min-width: 100px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

::v-deep .table table {
  table-layout: fixed;
  min-width: 600px;
}
</style>
