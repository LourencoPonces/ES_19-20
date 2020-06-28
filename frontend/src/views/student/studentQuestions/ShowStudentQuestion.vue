<template>
  <div>
    <!-- <span
      v-html="convertMarkDown(studentQuestion.content, studentQuestion.image)"
    /> -->
    <ckeditor
      outline
      auto-grow
      rows="1"
      v-model="studentQuestion.content"
      :config="editorConfig"
    >
    </ckeditor>
    <ul>
      <li v-for="option in studentQuestion.options" :key="option.number">
        <span
          v-if="option.correct"
          v-html="convertMarkDown('**[★]** ', null)"
        />
        <span
          v-html="convertMarkDown(option.content, null)"
          v-bind:class="[option.correct ? 'font-weight-bold' : '']"
        />
      </li>
    </ul>
    <br />
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import StudentQuestion from '@/models/management/StudentQuestion';

@Component
export default class ShowStudentQuestion extends Vue {
  @Prop({ type: StudentQuestion, required: true })
  readonly studentQuestion!: StudentQuestion;

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
