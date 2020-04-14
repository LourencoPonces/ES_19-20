import ClarificationRequestAnswer from './ClarificationRequestAnswer';

export default class ClarificationRequest {
  id!: number;
  questionId!: number;
  owner!: number;
  content!: string;
  answer: ClarificationRequestAnswer | null = null;

  constructor(jsonObj?: ClarificationRequest) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.questionId = jsonObj.questionId;
      this.content = jsonObj.content;
      this.owner = jsonObj.owner;

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
}
