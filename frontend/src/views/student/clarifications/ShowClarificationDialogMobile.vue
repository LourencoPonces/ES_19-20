<template>
  <v-dialog
    :value="dialog"
    @input="$emit('close-clarification-dialog', false)"
    @keydown.esc="$emit('close-clarification-dialog', false)"
    max-width="100%"
  >
    <v-card>
      <v-card-title class="justify-center">
        <span>
          {{ clarification.content }}<br />
          <span class="subtitle">{{ clarification.creationDate }} </span></span
        >
      </v-card-title>
      <v-card-text class="text-left">
        <v-row>
          <v-col cols="3">
            <b>Status:</b>
          </v-col>
          <v-col>
            <span>{{ clarification.status }}</span>
          </v-col>
        </v-row>
        <v-expansion-panels focusable v-if="clarification.hasMessages">
          <v-expansion-panel>
            <v-expansion-panel-header>
              <p>Messages</p>
            </v-expansion-panel-header>
            <v-expansion-panel-content>
              <clarification-thread
                :request="clarification"
                :isMobile="isMobile"
              ></clarification-thread>
            </v-expansion-panel-content>
          </v-expansion-panel>
        </v-expansion-panels>
        <div v-else style="text-align: center;">
          This request wasn't answered yet.
        </div>
      </v-card-text>
      <v-card-actions>
        <v-btn
          v-if="!clarification.hasMessages"
          fab
          color="error"
          small
          @click="$emit('delete-request', clarification)"
        >
          <v-icon medium class="mr-2">
            delete
          </v-icon>
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model } from 'vue-property-decorator';
import ClarificationRequest from '@/models/clarification/ClarificationRequest';
import ClarificationThread from '@/views/ClarificationThread.vue';
import ClarificationMessage from '@/models/clarification/ClarificationMessage';

@Component({
  components: {
    'clarification-thread': ClarificationThread
  }
})
export default class ShowQuestionDialogMobile extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Boolean, required: true }) isMobile!: boolean;
  @Prop({ type: ClarificationRequest, required: true })
  readonly clarification!: ClarificationRequest;

  statusList = ['PUBLIC', 'PRIVATE'];
}
</script>
<style>
.subtitle {
  color: gray;
  font-size: 11px;
}
</style>
