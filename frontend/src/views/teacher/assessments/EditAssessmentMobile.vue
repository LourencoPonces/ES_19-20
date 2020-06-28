<template>
  <v-dialog
    v-model="dialog"
    @input="$emit('close-edit-assessment-dialog', false)"
    max-width="100%"
  >
    <v-card v-if="assessment">
      <v-card-title class="justify-center">
        <span>{{ assessment.title }}<br /></span>
      </v-card-title>

      <v-card-text>
        <v-row>
          <v-col cols="4" align-self="center">
            <b>Title: </b>
          </v-col>
          <v-col>
            <v-text-field
              v-model="assessment.title"
              placeholder="assessment.title"
            >
            </v-text-field>
          </v-col>
        </v-row>
        <v-row>
          <v-col cols="4" align-self="center">
            <b>Order: </b>
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
          <v-col cols="4" align-self="center">
            <b>Status: </b>
          </v-col>
          <v-col>
            <v-select
              v-model="assessment.status"
              :items="statusList"
              dense
              @change="$emit('set-status', assessment.id, assessment.status)"
            >
              <template v-slot:selection="{ item }">
                <v-chip :color="getStatusColor(assessment.status)" small>
                  <span>{{ item }}</span>
                </v-chip>
              </template>
            </v-select>
          </v-col>
        </v-row>
        <v-expansion-panels
          v-if="
            assessment.topicConjunctions && assessment.topicConjunctions.length
          "
          focusable
        >
          <v-expansion-panel>
            <v-expansion-panel-header>Topics</v-expansion-panel-header>
            <v-expansion-panel-content>
              <v-list dense>
                <v-list-item
                  v-for="topicConjunction in assessment.topicConjunctions"
                  :key="topicConjunction.sequence"
                >
                  <v-list-item-content>
                    {{ getTopicsNames(topicConjunction) }}
                  </v-list-item-content>
                </v-list-item>
              </v-list>
            </v-expansion-panel-content>
          </v-expansion-panel>
        </v-expansion-panels>
        <span v-else>To select topics use the browser.</span>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          fab
          color="error"
          small
          @click="$emit('delete-assessment', assessment.id)"
        >
          <v-icon medium class="mr-2">
            delete
          </v-icon>
        </v-btn>
        <v-btn fab small color="primary" dark @click="$emit('save-assessment')">
          <v-icon medium class="mr-2">
            far fa-save
          </v-icon>
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import Assessment from '@/models/management/Assessment';
import TopicConjunction from '@/models/management/TopicConjunction';
import Topic from '@/models/management/Topic';

@Component
export default class EditAssessmentMobile extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Assessment, required: true }) readonly assessment!: Assessment;
  @Prop({ type: Array, required: true })
  readonly topicConjunctions!: TopicConjunction[];

  statusList = ['DISABLED', 'AVAILABLE', 'REMOVED'];
  allTopicsSearch: string = '';
  allTopicsSearchText: string = '';

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }

  getStatusColor(status: string) {
    if (status === 'REMOVED') return 'red';
    else if (status === 'DISABLED') return 'orange';
    else return 'green';
  }

  getTopicsNames(topicConjunction: TopicConjunction) {
    let final = '';
    topicConjunction.topics.forEach(topic => {
      final += topic.name + ' | ';
    });

    return final.substring(0, final.length - 3);
  }
}
</script>
<style>
.subtitle {
  color: gray;
  font-size: 11px;
}
</style>
