import Question from './Question';

export default class StudentQuestion extends Question {
  submittedStatus: string = 'WAITING_FOR_APPROVAL';
  justification: string = '';
  username!: string;

  constructor(jsonObj?: StudentQuestion) {
    super(jsonObj);
    this.status = 'DISABLED';
    if (jsonObj) {
      this.submittedStatus = jsonObj.submittedStatus;
      this.justification = jsonObj.justification;
      this.username = jsonObj.username;
      this.status = jsonObj.status;
    }
  }
}
