import { _ } from 'vue-underscore';

export default class ClarificationMessage {
  id!: number;
  requestId!: number;
  creatorId!: number;
  content!: string;
  creationDate: string;

  constructor(jsonObj?: ClarificationMessage) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.requestId = jsonObj.requestId;
      this.content = jsonObj.content;
      this.creatorId = jsonObj.creatorId;
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

  setOwnerId(id: number): void {
    this.creatorId = id;
  }

  getOwnerId(): number {
    return this.creatorId;
  }

  setContent(c: string): void {
    this.content = c;
  }

  getContent(): string {
    return this.content;
  }
}
