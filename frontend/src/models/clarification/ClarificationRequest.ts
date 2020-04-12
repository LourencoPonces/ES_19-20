import { _ } from 'vue-underscore';

export default class ClarificationRequest {
  id!: number;
  questionId!: number;
  owner!: number;
  content!: string;
  creationDate!: string;
  answer:/* ClarificationRequestAnswer | */null = null;
  

  constructor(jsonObj?: ClarificationRequest) {
    if (jsonObj) {
      this.id = jsonObj.id;
        this.questionId = jsonObj.questionId;
        this.content = jsonObj.content;
        this.owner = jsonObj.owner;
        this.creationDate = jsonObj.creationDate;

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

  getAnswerContent() : string {
    if (this.answer) {
   //   return this.answer.content;
      return "TODO ClarificationRequest, line 34";
    }

    return "No answer available.";
  }

  
}