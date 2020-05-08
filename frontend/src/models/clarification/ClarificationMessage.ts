import { _ } from 'vue-underscore';

export default class ClarificationMessage {
  id!: number;
  requestId!: number;
  creatorUsername!: string;
  content!: string;
  creationDate: string;

  constructor(jsonObj?: ClarificationMessage) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.requestId = jsonObj.requestId;
      this.content = jsonObj.content;
      this.creatorUsername = jsonObj.creatorUsername;
      this.creationDate = jsonObj.creationDate;
    }
  }

  getId(): number {
    return this.id;
  }

  setRequestId(id: number): void {
    this.requestId = id;
  }

  getRequestId(): number {
    return this.requestId;
  }

  setCreatorUsername(u: string): void {
    this.creatorUsername = u;
  }

  getCreatorUsername(): string {
    return this.creatorUsername;
  }

  setContent(c: string): void {
    this.content = c;
  }

  getContent(): string {
    return this.content;
  }
}
