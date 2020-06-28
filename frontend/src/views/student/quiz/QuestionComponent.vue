<template>
  <div class="question-container" v-if="question && question.options">
    <div class="question">
      <span
        v-if="backsies"
        class="square"
        @click="decreaseOrder"
        @mouseover="hover = true"
        @mouseleave="hover = false"
      >
        <i v-if="hover && questionOrder !== 0" class="fas fa-chevron-left" />
        <span v-else>{{ questionOrder + 1 }}</span>
      </span>
      <div>
        <ckeditor
          outline
          auto-grow
          rows="1"
          v-model="question.content"
          :config="editorConfig"
        >
        </ckeditor>
      </div>
      <div class="square" @click="increaseOrder" data-cy="nextQuestion">
        <i
          v-if="questionOrder !== questionNumber - 1"
          class="fas fa-chevron-right"
        />
      </div>
    </div>
    <ul class="option-list">
      <li
        v-for="(n, index) in question.options.length"
        :key="index"
        v-bind:class="[
          'option',
          optionId === question.options[index].optionId ? 'selected' : ''
        ]"
        @click="selectOption(question.options[index].optionId)"
        data-cy="options"
      >
        <span class="option-letter">{{ optionLetters[index] }}</span>
        <span
          class="option-content"
          v-html="convertMarkDown(question.options[index].content)"
        />
      </li>
    </ul>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model, Emit } from 'vue-property-decorator';
import StatementQuestion from '@/models/statement/StatementQuestion';
import Image from '@/models/management/Image';
import { convertMarkDown } from '@/services/ConvertMarkdownService';

@Component
export default class QuestionComponent extends Vue {
  @Model('questionOrder', Number) questionOrder: number | undefined;
  @Prop(StatementQuestion) readonly question: StatementQuestion | undefined;
  @Prop(Number) optionId: number | undefined;
  @Prop() readonly questionNumber!: number;
  @Prop() readonly backsies!: boolean;
  hover: boolean = false;
  optionLetters: string[] = ['A', 'B', 'C', 'D'];

  @Emit()
  increaseOrder() {
    return 1;
  }

  @Emit()
  decreaseOrder() {
    return 1;
  }

  @Emit()
  selectOption(optionId: number) {
    return optionId;
  }

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

<style lang="scss" scoped />
