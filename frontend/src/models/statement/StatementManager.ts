import StatementQuestion from '@/models/statement/StatementQuestion';
import StatementCorrectAnswer from '@/models/statement/StatementCorrectAnswer';
import StatementAnswer from '@/models/statement/StatementAnswer';
import RemoteServices from '@/services/RemoteServices';
import StatementQuiz from '@/models/statement/StatementQuiz';
import Store from '@/store';

export default class StatementManager {
  questionType: string = 'all';
  assessment: string = 'all';
  // topic: string[] = [];
  numberOfQuestions: string = '5';
  statementQuiz: StatementQuiz | null = null;
  correctAnswers: StatementCorrectAnswer[] = [];

  private static _quiz: StatementManager = new StatementManager();

  static get getInstance(): StatementManager {
    return this._quiz;
  }

  async getQuizStatement() {
    let params = {
      // topic: this.topic,
      questionType: this.questionType,
      assessment: this.assessment,
      numberOfQuestions: +this.numberOfQuestions
    };

    this.statementQuiz = await RemoteServices.generateStatementQuiz(params);
  }

  async getCorrectAnswers() {
    if (this.statementQuiz) {
      this.correctAnswers = await RemoteServices.concludeQuiz(
        this.statementQuiz.id
      );
    } else {
      throw Error('No quiz');
    }
  }

  reset() {
    this.statementQuiz = null;
    this.correctAnswers = [];
  }

  isEmpty(): boolean {
    return this.statementQuiz == null;
  }
}
