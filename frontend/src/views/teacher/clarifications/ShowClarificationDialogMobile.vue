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
          <v-col cols="4">
            <b>Status:</b>
          </v-col>
          <v-col>
            <v-select
              v-model="clarification.status"
              :items="statusList"
              dense
              @change="$emit('change-clarification-status', clarification)"
            >
              <template v-slot:selection="{ item }">
                <span>{{ item }}</span>
              </template>
            </v-select>
          </v-col>
        </v-row>
        <v-expansion-panels focusable>
          <v-expansion-panel>
            <v-expansion-panel-header>
              <p>Question</p>
            </v-expansion-panel-header>
            <v-expansion-panel-content>
              <show-question :question="question" />
            </v-expansion-panel-content>
          </v-expansion-panel>
          <v-expansion-panel v-if="clarification.hasMessages">
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
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model } from 'vue-property-decorator';
import Question from '@/models/management/Question';
import ShowQuestion from '@/views/teacher/questions/ShowQuestion.vue';
import ClarificationRequest from '@/models/clarification/ClarificationRequest';
import ClarificationThread from '@/views/ClarificationThread.vue';
import ClarificationMessage from '@/models/clarification/ClarificationMessage';

@Component({
  components: {
    'show-question': ShowQuestion,
    'clarification-thread': ClarificationThread
  }
})
export default class ShowQuestionDialogMobile extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Boolean, required: true }) isMobile!: boolean;
  @Prop({ type: Question, required: true }) readonly question!: Question;
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
