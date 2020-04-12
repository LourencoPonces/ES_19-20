import { _ } from 'vue-underscore';

export default class ClarificationRequest {
  questionId!: number;
  owner!: number;
  content!: string;
  answer:/* ClarificationRequestAnswer | */null = null;

  constructor(jsonObj?: ClarificationRequest) {
    if (jsonObj) {
        this.questionId = jsonObj.questionId;
        this.content = jsonObj.content;
        this.owner = jsonObj.owner;

  /*      if (jsonObj.answer) {     // TODO complete when answer is done
          this.answer = jsonObj.answer;
        }
      */ }
  }

  hasAnswer() : boolean { return this.answer != null; }

  setQuestionId(id : number) : void { this.questionId = id; }

  getQuestionId() : number { return this.questionId; }

  setOwnerId(id : number) : void { this.owner = id; }

  setContent(c : string) : void { this.content = c; }

  
}