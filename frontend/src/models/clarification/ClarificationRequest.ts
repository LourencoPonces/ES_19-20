import ClarificationRequestAnswer from './ClarificationRequestAnswer';

export default class ClarificationRequest {
  id!: number;
  questionId!: number;
  owner!: number;
  creationDate!: string;
  content!: string;
  answer: ClarificationRequestAnswer | null = null;

  constructor(jsonObj?: ClarificationRequest) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.questionId = jsonObj.questionId;
      this.content = jsonObj.content;
      this.owner = jsonObj.owner;
      this.creationDate = jsonObj.creationDate;

      if (jsonObj.answer) {
        this.answer = new ClarificationRequestAnswer(jsonObj.answer);
      }
    }
  }

  newAnswer(): ClarificationRequestAnswer {
    const answer = new ClarificationRequestAnswer();
    answer.setRequestId(this.id);
    return answer;
  }

  hasAnswer(): boolean {
    return this.answer != null;
  }

  getAnswer(): ClarificationRequestAnswer {
    if (!this.answer) throw Error('answer unavailable');
    return this.answer as ClarificationRequestAnswer;
  }

  setAnswer(answer: ClarificationRequestAnswer | null) {
    this.answer = answer;
  }

  setQuestionId(id: number): void {
    this.questionId = id;
  }

  getQuestionId(): number {
    return this.questionId;
  }

  setOwnerId(id: number): void {
    this.owner = id;
  }

  getOwnerId(): number {
    return this.owner;
  }

  setContent(c: string): void {
    this.content = c;
  }

  getAnswerContent(): string | void {
    return this.answer?.getContent();
  }
}
