<template>
  <div class="quiz-container" v-if="statementManager.correctAnswers.length > 0">
    <div class="question-navigation">
      <div class="navigation-buttons">
        <span
          v-for="index in +statementManager.statementQuiz.questions.length"
          v-bind:class="[
            'question-button',
            index === questionOrder + 1 ? 'current-question-button' : '',
            index === questionOrder + 1 &&
            statementManager.correctAnswers[index - 1].correctOptionId !==
              statementManager.statementQuiz.answers[index - 1].optionId
              ? 'incorrect-current'
              : '',
            statementManager.correctAnswers[index - 1].correctOptionId !==
            statementManager.statementQuiz.answers[index - 1].optionId
              ? 'incorrect'
              : ''
          ]"
          :key="index"
          @click="changeOrder(index - 1)"
        >
          {{ index }}
        </span>
      </div>
      <span
        class="left-button"
        @click="decreaseOrder"
        v-if="questionOrder !== 0"
        ><i class="fas fa-chevron-left"
      /></span>
      <span
        class="right-button"
        @click="increaseOrder"
        v-if="
          questionOrder !== statementManager.statementQuiz.questions.length - 1
        "
        ><i class="fas fa-chevron-right"
      /></span>
    </div>
    <result-component
      v-model="questionOrder"
      :answer="statementManager.statementQuiz.answers[questionOrder]"
      :correctAnswer="statementManager.correctAnswers[questionOrder]"
      :question="statementManager.statementQuiz.questions[questionOrder]"
      :questionNumber="statementManager.statementQuiz.questions.length"
      :clarifications="statementManager.statementQuiz.questions[questionOrder].clarifications"
      @increase-order="increaseOrder"
      @decrease-order="decreaseOrder"
    />

    <discussion-component 
      :question="statementManager.statementQuiz.questions[questionOrder]"
      :clarifications="statementManager.statementQuiz.questions[questionOrder].clarifications"
      @submit-request="submitRequest"
    /> 
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import StatementManager from '@/models/statement/StatementManager';
import ResultComponent from '@/views/student/quiz/ResultComponent.vue';
import DiscussionComponent from '@/views/student/quiz/DiscussionComponent.vue';
import ClarificationRequest from '../../../models/clarification/ClarificationRequest';
import RemoteServices from '../../../services/RemoteServices';
import StatementQuiz from '../../../models/statement/StatementQuiz';

@Component({
  components: {
    'result-component': ResultComponent,
    'discussion-component': DiscussionComponent
  }
})
export default class ResultsView extends Vue {
  statementManager: StatementManager = StatementManager.getInstance;
  questionOrder: number = 0;

  async created() {
    if (this.statementManager.isEmpty()) {
      await this.$router.push({ name: 'create-quiz' });
    } else if (this.statementManager.correctAnswers.length === 0) {
      await this.$store.dispatch('loading');
      setTimeout(() => {
        this.statementManager.concludeQuiz();
      }, 2000);

      await this.$store.dispatch('clearLoading');
    }
  }
 
  increaseOrder(): void {
    if (
      this.questionOrder + 1 <
      +this.statementManager.statementQuiz!.questions.length
    ) {
      this.questionOrder += 1;
    }
  }

  decreaseOrder(): void {
    if (this.questionOrder > 0) {
      this.questionOrder -= 1;
    }
  }

  changeOrder(n: number): void {
    if (n >= 0 && n < +this.statementManager.statementQuiz!.questions.length) {
      this.questionOrder = n;
    }
  }

  async submitRequest(info : string[]) {   
    try {
      let req = this.createRequest(info[0], this.$store.getters.getUserId, parseInt(info[1]));
      this.statementManager.addClarificationRequest(this.questionOrder, await RemoteServices.submitClarificationRequest(req));
    } 
    catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

    createRequest(content: string, owner: number, question: number) : ClarificationRequest {
    var req = new ClarificationRequest();
    req.setQuestionId(question);
    req.setOwnerId(owner)
    req.setContent(content);
    return req;
  }
}
</script>

<style lang="scss" scoped>
.incorrect {
  color: #cf2323 !important;
}

.incorrect-current {
  background-color: #cf2323 !important;
  color: #fff !important;
}
</style>
