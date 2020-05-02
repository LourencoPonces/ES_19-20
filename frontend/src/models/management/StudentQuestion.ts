import Question from './Question';

const internalStatuses = {
  REJECTED: 'Rejected',
  APPROVED: 'Approved',
  WAITING_FOR_APPROVAL: 'Waiting for Approval'
};

const externalStatuses = {
  REJECTED: 'REJECTED',
  APPROVED: 'APPROVED',
  WAITING_FOR_APPROVAL: 'WAITING_FOR_APPROVAL'
};

export default class StudentQuestion extends Question {
  submittedStatus: string = internalStatuses.WAITING_FOR_APPROVAL;
  justification: string = '';
  username!: string;

  constructor(jsonObj?: StudentQuestion) {
    super(jsonObj);
    this.status = 'DISABLED';
    if (jsonObj) {
      // in the frontend use lower letter format. when passing to the backend, capitalize
      this.submittedStatus = StudentQuestion.getFrontendStatusFormat(
        jsonObj.submittedStatus
      );
      this.justification = jsonObj.justification;
      this.username = jsonObj.username;
      this.status = jsonObj.status;
    }
  }

  static getFrontendStatusFormat(status: string): string {
    switch (status) {
      case externalStatuses.WAITING_FOR_APPROVAL:
        return internalStatuses.WAITING_FOR_APPROVAL;
      case externalStatuses.APPROVED:
        return internalStatuses.APPROVED;
      case externalStatuses.REJECTED:
        return internalStatuses.REJECTED;

      // case already internal
      case internalStatuses.WAITING_FOR_APPROVAL:
      case internalStatuses.APPROVED:
      case internalStatuses.REJECTED:
        return status;
      default:
        throw new Error('Invalid status');
    }
  }

  static getServerStatusFormat(status: string): string {
    switch (status) {
      case internalStatuses.WAITING_FOR_APPROVAL:
        return externalStatuses.WAITING_FOR_APPROVAL;
      case internalStatuses.APPROVED:
        return externalStatuses.APPROVED;
      case internalStatuses.REJECTED:
        return externalStatuses.REJECTED;

      // case already external
      case externalStatuses.WAITING_FOR_APPROVAL:
      case externalStatuses.APPROVED:
      case externalStatuses.REJECTED:
        return status;
      default:
        throw new Error('Invalid status');
    }
  }

  getEvaluationColor(): string {
    switch (this.submittedStatus) {
      case internalStatuses.WAITING_FOR_APPROVAL:
        return 'orange';
      case internalStatuses.APPROVED:
        return 'green';
      case internalStatuses.REJECTED:
        return 'red';
      default:
        throw new Error('Invalid status');
    }
  }

  static toRequest(sq: StudentQuestion) {
    let req = new StudentQuestion(sq);
    req.submittedStatus = this.getServerStatusFormat(sq.submittedStatus);
    return req;
  }

  isEditable(): boolean {
    return (
      this.isChangeable() || this.submittedStatus === internalStatuses.REJECTED
    );
  }

  isChangeable(): boolean {
    return this.submittedStatus === internalStatuses.WAITING_FOR_APPROVAL;
  }
}
