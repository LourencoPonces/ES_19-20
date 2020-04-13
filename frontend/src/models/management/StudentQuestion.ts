import Question from './Question';

export default class StudentQuestion extends Question {
  submittedStatus: string = 'WAITING_FOR_APPROVAL';
  justification: string = '';
  username!: string;

  constructor(jsonObj?: StudentQuestion) {
    super(jsonObj);
    this.status = 'DISABLED';
    if (jsonObj) {
      // in the frontend use lower letter format. when passing to the backend, capitalize
      this.submittedStatus = this.getSubmittedStatus(jsonObj.submittedStatus);
      this.justification = jsonObj.justification;
      this.username = jsonObj.username;
      this.status = jsonObj.status;
    }
  }

  getSubmittedStatus(submittedStatus: string): string {
    if (submittedStatus === 'APPROVED') return 'Approved';
    if (submittedStatus === 'REJECTED') return 'Rejected';
    return 'Waiting for Approval';
  }

  static toRequest(sq: StudentQuestion) {
    let req = new StudentQuestion(sq);
    req.submittedStatus = this.getServerStatusFormat(sq.submittedStatus);
    return req;
  }

  static getServerStatusFormat(status: string): string {
    console.log('Translating');
    if (status === 'Waiting for Approval') return 'WAITING_FOR_APPROVAL';
    if (status === 'Rejected') return 'REJECTED';
    if (status === 'Approved') return 'APPROVED';
    return '';
  }
}
