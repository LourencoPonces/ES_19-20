<template>
  <div class="container">
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
                <b v-if="messageIsMine(msg)">(me)</b>
              </v-row>
              <v-row>{{ msg.creationDate }}</v-row>
            </v-container>
          </v-col>
          <v-col class="ml-auto" cols="auto">
            <v-tooltip bottom v-if="messageIsMine(msg)">
              <template v-slot:activator="{ on }">
                <v-icon
                  small
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
    <v-container fluid>
      <v-row>
        <v-textarea
          label="Message Content"
          v-model="newMessageContent"
          data-cy="newMessageContent"
        />
      </v-row>
      <v-row>
        <v-switch v-model="newMessageResolvedState" label="Resolved" />
        <v-spacer />
        <v-btn primary @click="submitMessage()">
          Submit Message
        </v-btn>
      </v-row>
    </v-container>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import ClarificationRequest from '../models/clarification/ClarificationRequest';
import RemoteServices from '../services/RemoteServices';
import ClarificationMessage from '../models/clarification/ClarificationMessage';
import UserNameCacheService from '../services/UserNameCacheService';

@Component
export default class ClarificationThread extends Vue {
  @Prop(ClarificationRequest) request!: ClarificationRequest;

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
  padding: 10px;
}
.message {
  padding: 10px;
  margin-bottom: 20px;
  margin-top: 20px;
}
.message-content {
  margin: 10px;
}
</style>
