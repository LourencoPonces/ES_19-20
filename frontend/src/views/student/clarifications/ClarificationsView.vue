<template>
  <div class="container">
    <h2>Clarification Requests</h2>
    <!-- BROWSER -->
    <v-card v-if="!isMobile">
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
            medium
            class="mr-2"
            :data-cy="'private-' + item.content.slice(0, 15)"
            >fas fa-eye-slash</v-icon
          >
          <v-icon
            v-else
            medium
            class="mr-2"
            :data-cy="'public-' + item.content.slice(0, 15)"
            >fas fa-eye</v-icon
          >
        </template>

        <template v-slot:item.content="{ item }">
          <div
            class="short-content"
            :data-cy="'req-' + item.content.slice(0, 15)"
          >
            {{ item.content }}
          </div>
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
                medium
                class="mr-2"
                v-on="on"
                @click="deleteRequest(item)"
                color="red"
                data-cy="deleteRequest"
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
    <!-- MOBILE -->
    <v-card v-else>
      <v-data-table
        :headers="headers_mobile"
        :items="requests"
        :search="search"
        :sort-by="['creationDate']"
        sort-desc
        :mobile-breakpoint="0"
        :items-per-page="15"
        :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
        fixed-header
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
          <v-row @click="showClarificationDialog(item)">
            <v-col cols="2" align-self="center" color="green">
              <v-icon v-if="item.resolved">far fa-check-square</v-icon>
              <v-icon v-else>far fa-square</v-icon>
            </v-col>
            <v-col cols="9" align-self="center">
              <p>{{ item.content }}</p>
            </v-col>
          </v-row>
        </template>
      </v-data-table>

      <show-clarification-dialog-mobile
        v-if="currentClarification"
        v-model="clarificationDialogMobile"
        :clarification="currentClarification"
        :isMobile="isMobile"
        v-on:close-clarification-dialog="closeClarificationDialog"
        v-on:delete-request="deleteRequest"
      />
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import ClarificationRequest from '@/models/clarification/ClarificationRequest';
import Question from '@/models/management/Question';
import ShowClarificationDialogMobile from '@/views/student/clarifications/ShowClarificationDialogMobile.vue';
import ClarificationThread from '@/views/ClarificationThread.vue';

@Component({
  components: {
    'clarification-thread': ClarificationThread,
    'show-clarification-dialog-mobile': ShowClarificationDialogMobile
  }
})
export default class ClarificationsView extends Vue {
  isMobile: boolean = false;
  requests: ClarificationRequest[] = [];
  search: string = '';
  newContent: string = '';
  editingItem: ClarificationRequest | null = null;
  dialog: boolean = false;
  clarificationDialogMobile: boolean = false;
  currentClarification: ClarificationRequest | null = null;

  headers: object = [
    {
      text: 'Actions',
      value: 'action',
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
      text: 'Request',
      value: 'content',
      align: 'left'
    },
    {
      text: 'Resolved',
      value: 'resolved',
      align: 'center',
      sortable: false,
      width: '110px'
    },
    {
      text: 'Creation Date',
      value: 'creationDate',
      align: 'center',
      width: '150px'
    }
  ];

  headers_mobile: object = [
    {
      text: 'Clarification Request',
      value: 'content',
      align: 'center',
      sortable: false
    }
  ];

  async created() {
    await this.$store.dispatch('loading');
    if (window.innerWidth <= 500) {
      this.isMobile = true;
    }
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

  showClarificationDialog(clarification: ClarificationRequest) {
    this.currentClarification = clarification;
    this.clarificationDialogMobile = true;
  }

  closeClarificationDialog() {
    this.currentClarification = null;
    this.clarificationDialogMobile = false;
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
  white-space: pre-wrap;
}

.short-content {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

::v-deep .table table {
  table-layout: fixed;
  min-width: 600px;
}
</style>
