<template>
  <!-- BROWSER -->
  <div v-if="!isMobile" class="container">
    <v-card v-for="msg in request.messages" v-bind:key="msg.id" class="message">
      <v-container fluid>
        <v-row class="align-center">
          <v-col cols="auto">
            <v-avatar :size="AVATAR_SIZE + 'px'">
              <img :src="imgForUsername(msg.creatorUsername)" alt="avatar" />
            </v-avatar>
          </v-col>
          <v-col cols="auto">
            <v-container fluid>
              <v-row>
                {{ nameForUsername(msg.creatorUsername) }}
                <b v-if="messageIsMine(msg)" class="ml-1">(me)</b>
              </v-row>
              <v-row>{{ msg.creationDate }}</v-row>
            </v-container>
          </v-col>
          <v-col class="ml-auto" cols="auto">
            <v-tooltip bottom v-if="messageIsMine(msg)">
              <template v-slot:activator="{ on }">
                <v-icon
                  class="mr-2"
                  v-on="on"
                  @click="deleteMessage(msg)"
                  color="red"
                  :data-cy="'deleteMessage-' + msg.content.slice(0, 15)"
                  aria-label="delete message"
                  >delete</v-icon
                >
              </template>
              Delete Message
            </v-tooltip>
          </v-col>
        </v-row>
        <v-row class="message-content">
          {{ msg.content }}
        </v-row>
      </v-container>
    </v-card>
    <span v-if="!request.hasMessages">No messages (yet)</span>
    <v-container fluid class="new-msg-container">
      <v-row>
        <v-textarea
          label="Message Content"
          v-model="newMessageContent"
          data-cy="newMessageContent"
        />
      </v-row>
      <v-row>
        <v-switch
          v-model="newMessageResolvedState"
          label="Resolved"
          data-cy="newMessageUpdateResolved"
        />
        <v-spacer />
        <v-btn
          primary
          dark
          color="blue"
          data-cy="newMessageSubmit"
          @click="submitMessage()"
        >
          Submit Message
        </v-btn>
      </v-row>
    </v-container>
  </div>
  <!-- MOBILE -->
  <div v-else class="container">
    <v-card
      flat
      max-width="100%"
      v-for="msg in request.messages"
      v-bind:key="msg.id"
    >
      <v-card-title class="message-author">
        {{ nameForUsername(msg.creatorUsername) }}
        <b v-if="messageIsMine(msg)" class="ml-1">(me)</b>
      </v-card-title>
      <v-card-subtitle>{{ msg.creationDate }}</v-card-subtitle>
      <v-card-text>
        {{ msg.content }}
      </v-card-text>
      <v-card-actions>
        <v-btn
          v-if="messageIsMine(msg)"
          fab
          color="error"
          small
          @click="deleteMessage(msg)"
        >
          <v-icon class="mr-2" medium>
            delete
          </v-icon>
        </v-btn>
      </v-card-actions>
    </v-card>
    <v-divider />
    <v-container class="new-msg-container">
      <v-row>
        <v-textarea
          label="Message Content"
          v-model="newMessageContent"
          data-cy="newMessageContent"
        />
      </v-row>
      <v-row>
        <v-col cols="7">
          <v-switch v-model="newMessageResolvedState" label="Resolved" />
        </v-col>
        <v-col cols="3" align-self="center">
          <v-btn primary small color="primary" @click="submitMessage()">
            Submit
          </v-btn>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import ClarificationRequest from '@/models/clarification/ClarificationRequest';
import RemoteServices from '@/services/RemoteServices';
import ClarificationMessage from '@/models/clarification/ClarificationMessage';
import UserNameCacheService from '@/services/UserNameCacheService';

@Component
export default class ClarificationThread extends Vue {
  @Prop(ClarificationRequest) request!: ClarificationRequest;
  @Prop({ type: Boolean, required: true }) isMobile!: boolean;

  newMessageContent: string = '';
  newMessageResolvedState: boolean = false;

  myUsername: string = '';

  readonly AVATAR_SIZE: number = 52;

  beforeMount() {
    this.newMessageResolvedState = this.request.getResolved();
    this.myUsername = this.$store.getters.getUser.username;
  }

  async submitMessage(): Promise<void> {
    try {
      const msg = await RemoteServices.submitClarificationMessage(
        this.request.getId(),
        this.newMessageContent,
        this.newMessageResolvedState
      );
      this.request.addMessage(msg);
      this.request.resolved = this.newMessageResolvedState;

      this.newMessageContent = '';
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  async deleteMessage(msg: ClarificationMessage): Promise<void> {
    try {
      await RemoteServices.deleteClarificationMessage(msg);
      this.request.removeMessage(msg);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  messageIsMine(msg: ClarificationMessage): boolean {
    return msg.getCreatorUsername() == this.myUsername;
  }

  imgForUsername(username: string): string {
    if (username.startsWith('ist')) {
      return `https://fenix.tecnico.ulisboa.pt/user/photo/${username}?size=${this.AVATAR_SIZE}`;
    } else {
      return 'https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png';
    }
  }

  nameForUsername(username: string): string {
    return UserNameCacheService.getName(username);
  }
}
</script>

<style scoped lang="scss">
.container {
  /*
    scoped styles dont' seem to work with sass very well
    ClarificationsView stuff is bleeding through
    this resets things to normal
  */
  max-width: initial !important;
  margin: 0 !important;
  padding: 0 !important;
}

.message {
  padding: 10px;
  margin-bottom: 20px;
  margin-top: 20px;
}

.message-content {
  padding: 0 12px;
  white-space: pre-wrap;
}

.message-author {
  font-size: 16px;
}

.new-msg-container > * {
  /* negative margins in v-row mess up styles */
  margin-left: 0;
  margin-right: 0;
}
</style>
