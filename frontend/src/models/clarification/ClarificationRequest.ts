import ClarificationRequestAnswer from './ClarificationRequestAnswer';

export default class ClarificationRequest {
  id!: number;
  questionId!: number;
  owner!: number;
  content!: string;
<<<<<<< HEAD
  creationDate!: string;
  answer:/* ClarificationRequestAnswer | */null = null;
  
=======
  answer: ClarificationRequestAnswer | null = null;
>>>>>>> 5d584bce... Expose clarification answers to frontend

  constructor(jsonObj?: ClarificationRequest) {
    if (jsonObj) {
      this.id = jsonObj.id;
<<<<<<< HEAD
        this.questionId = jsonObj.questionId;
        this.content = jsonObj.content;
        this.owner = jsonObj.owner;
        this.creationDate = jsonObj.creationDate;

  /*      if (jsonObj.answer) {     // TODO complete when answer is done
          this.answer = jsonObj.answer;
        }
      */ }
=======
      this.questionId = jsonObj.questionId;
      this.content = jsonObj.content;
      this.owner = jsonObj.owner;

      /*
      if (jsonObj.answer) {     // TODO complete when answer is done
        this.answer = jsonObj.answer;
      }
      */
    }
>>>>>>> 5d584bce... Expose clarification answers to frontend
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

<<<<<<< HEAD
  getAnswerContent() : string {
    if (this.answer) {
   //   return this.answer.content;
      return "TODO ClarificationRequest, line 34";
    }

    return "No answer available.";
  }

  
}
=======
  setContent(c: string): void {
    this.content = c;
  }
}
>>>>>>> 5d584bce... Expose clarification answers to frontend
