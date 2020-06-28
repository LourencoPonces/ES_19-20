<template>
  <div>
    <ckeditor
      outline
      auto-grow
      rows="1"
      v-model="question.content"
      :config="editorConfig"
    >
    </ckeditor>
    <br />
    <ul>
      <li v-for="option in question.options" :key="option.number">
        <span
          v-if="option.correct"
          v-html="convertMarkDown('**[★]** ' + option.content)"
          v-bind:class="[option.correct ? 'font-weight-bold' : '']"
        />
        <span v-else v-html="convertMarkDown(option.content)" />
      </li>
    </ul>
    <br />
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Question from '@/models/management/Question';
import Image from '@/models/management/Image';

@Component
export default class ShowQuestion extends Vue {
  @Prop({ type: Question, required: true }) readonly question!: Question;

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }

  data() {
    return {
      editorConfig: {
        removePlugins: 'elementspath',
        readOnly: true,
        toolbarCanCollapse: true,
        toolbarStartupExpanded: false,
        language: 'en',
        extraPlugins: 'mathjax',
        mathJaxLib:
          'https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.4/MathJax.js?config=TeX-AMS_HTML'
      }
    };
  }
}
</script>
