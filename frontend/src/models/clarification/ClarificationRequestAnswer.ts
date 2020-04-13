import { _ } from 'vue-underscore';

export default class ClarificationRequestAnswer {
  requestId!: number;
  owner!: number;
  content!: string;

  constructor(jsonObj?: ClarificationRequestAnswer) {
    if (jsonObj) {
      this.requestId = jsonObj.requestId;
      this.content = jsonObj.content;
      this.owner = jsonObj.owner;
    }
  }

  setRequestId(id: number): void {
    this.requestId = id;
  }

  getRequestId(): number {
    return this.requestId;
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

  getContent(): string {
    return this.content;
  }
}
