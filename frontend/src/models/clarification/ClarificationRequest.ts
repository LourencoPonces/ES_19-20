import ClarificationMessage from './ClarificationMessage';

export default class ClarificationRequest {
  id!: number;
  questionId!: number;
  creatorUsername!: string;
  creationDate!: string;
  content!: string;
  messages: ClarificationMessage[];
  status!: string;
  resolved!: boolean;

  constructor(jsonObj?: ClarificationRequest) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.questionId = jsonObj.questionId;
      this.content = jsonObj.content;
      this.creatorUsername = jsonObj.creatorUsername;
      this.creationDate = jsonObj.creationDate;
      this.status = jsonObj.status;
      this.messages = jsonObj.messages.map(msg => new ClarificationMessage(msg));
      this.resolved = jsonObj.resolved;
    }
  }

  newMessage(): ClarificationMessage {
    const message = new ClarificationMessage();
    message.setRequestId(this.id);
    return message;
  }

  get hasMessages(): boolean {
    return this.messages.length > 0;
  }

  getId(): number {
    return this.id;
  }

  getMessages(): ClarificationMessage[] {
    return this.messages;
  }

  addMessage(msg: ClarificationMessage) {
    this.messages.push(msg);
  }

  setQuestionId(id: number): void {
    this.questionId = id;
  }

  getQuestionId(): number {
    return this.questionId;
  }

  getCreatorUsername(): string {
    return this.creatorUsername;
  }

  setCreatorUsername(username: string): void {
    this.creatorUsername = username;
  }

  setContent(c: string): void {
    this.content = c;
  }

  getContent(): string {
    return this.content;
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
