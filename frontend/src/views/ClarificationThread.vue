<template>
  <div>
    <v-card v-for="msg in request.messages" v-bind:key="msg.id">
      <v-avatar :size="AVATAR_SIZE + 'px'">
        <img :src="imgForUsername(msg.creatorUsername)" alt="avatar" />
      </v-avatar>
      <v-tooltip bottom>
        <template v-slot:activator="{ on }">
          <v-icon
            small
            class="mr-2"
            v-on="on"
            @click="deleteMessage(msg)"
            color="red"
            :data-cy="'deleteMessage-' + msg.content.slice(0, 15)"
            >delete</v-icon
          >
        </template>
      </v-tooltip>
      <div>{{ msg.creationDate }}</div>
      <div>{{ msg.content }}</div>
    </v-card>
    <div>
      <v-textarea
        v-model="newMessageContent"
        data-cy="newMessageContent"
      ></v-textarea>
      <v-checkbox v-model="newMessageResolvedState"></v-checkbox>
      <v-btn primary @click="submitMessage()">
        Submit Answer
      </v-btn>
    </div>
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

  readonly AVATAR_SIZE: number = 52;

  attached() {
    this.newMessageResolvedState = this.request.getResolved();
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

<style scoped lang="scss"></style>
