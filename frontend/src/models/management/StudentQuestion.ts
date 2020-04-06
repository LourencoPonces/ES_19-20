import Question from './Question';

export default class StudentQuestion extends Question {
  submittedStatus: string = 'WAITING_FOR_APPROVAL';
  justification: string = '';
  username!: number;

  constructor(jsonObj?: StudentQuestion) {
    super(jsonObj);
    if (jsonObj) {
      this.submittedStatus = jsonObj.status;
      this.justification = jsonObj.justification;
      this.username = jsonObj.username;
    }
  }
}
