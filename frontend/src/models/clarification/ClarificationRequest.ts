import ClarificationRequestAnswer from './ClarificationRequestAnswer';

export default class ClarificationRequest {
  id!: number;
  questionId!: number;
  owner!: number;
  creationDate!: string;
  content!: string;
  answer: ClarificationRequestAnswer | null = null;
  status!: string;

  constructor(jsonObj?: ClarificationRequest) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.questionId = jsonObj.questionId;
      this.content = jsonObj.content;
      this.owner = jsonObj.owner;
      this.creationDate = jsonObj.creationDate;
      this.status = jsonObj.status;

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

  get hasAnswer(): boolean {
    return this.answer != null;
  }

  getId(): number {
    return this.id;
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

  setContent(c: string): void {
    this.content = c;
  }

  getContent(): string {
    return this.content;
  }

  getAnswerContent(): string | void {
    return this.answer?.getContent();
  }

  isPrivate(): boolean {
    return this.status == 'PRIVATE';
  }

  setStatus(status: string): void {
    this.status = status;
  }

  getStatus(): string {
    return this.status;
  }
}
