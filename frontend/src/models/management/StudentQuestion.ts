import Question from './Question';

export const internalStatuses = {
  REJECTED: 'Rejected',
  APPROVED: 'Approved',
  WAITING_FOR_APPROVAL: 'Waiting for Approval',
  PROMOTED: 'Promoted'
};

const externalStatuses = {
  REJECTED: 'REJECTED',
  APPROVED: 'APPROVED',
  WAITING_FOR_APPROVAL: 'WAITING_FOR_APPROVAL',
  PROMOTED: 'PROMOTED'
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
      case externalStatuses.PROMOTED:
        return internalStatuses.PROMOTED;

      // case already internal
      case internalStatuses.WAITING_FOR_APPROVAL:
      case internalStatuses.APPROVED:
      case internalStatuses.REJECTED:
      case internalStatuses.PROMOTED:
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
      case internalStatuses.PROMOTED:
        return externalStatuses.PROMOTED;

      // case already external
      case externalStatuses.WAITING_FOR_APPROVAL:
      case externalStatuses.APPROVED:
      case externalStatuses.REJECTED:
      case externalStatuses.PROMOTED:
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
      case internalStatuses.PROMOTED:
        return 'blue';
      default:
        throw new Error('Invalid status');
    }
  }

  static toRequest(sq: StudentQuestion) {
    let req = new StudentQuestion(sq);
    req.submittedStatus = this.getServerStatusFormat(sq.submittedStatus);
    return req;
  }

  canEvaluate(): boolean {
    return this.submittedStatus !== internalStatuses.PROMOTED;
  }

  isChangeable(): boolean {
    return (
      this.submittedStatus === internalStatuses.REJECTED ||
      (this.numberOfAnswers === 0 &&
        this.submittedStatus === internalStatuses.WAITING_FOR_APPROVAL)
    );
  }

  // get status representation
  static getRejected(): string {
    return internalStatuses.REJECTED;
  }
  static getApproved(): string {
    return internalStatuses.APPROVED;
  }
  static getWaitingForApproval(): string {
    return internalStatuses.WAITING_FOR_APPROVAL;
  }
}
