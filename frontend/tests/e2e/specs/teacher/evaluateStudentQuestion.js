let APPROVED = 'Approved'
let REJECTED = 'Rejected'
let WAITING_FOR_APPROVAL = 'Waiting for Approval'

describe('Student Question Evaluation', () => {
  let questionTitle = 'Question #' + Date.now().toString();
  let questionContent = 'To be or not to be?';
  let topics = ['Adventure Builder'];
  let options = ['AAAA', 'BBBB', 'CCCC', 'DDDD'];

  beforeEach(() => {
    questionTitle = 'Question #' + Date.now().toString();
    // log in as student
    cy.demoStudentLogin();

    // create student question
    cy.get('[data-cy="my-area"]').click();
    cy.get('[data-cy="student-questions"]').click();
    cy.createStudentQuestion(
      questionTitle,
      questionContent,
      topics,
      options,
      1
    );

    // logout
    cy.contains('Logout').click();

    // log in as teacher
    cy.demoTeacherLogin();
  });

  afterEach(() => {
    cy.contains('Logout').click();
  });

  it('Approve a student question without justification', () => {
    let justification = null;
    let prevStatus = WAITING_FOR_APPROVAL;
    let status = APPROVED;

    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.evaluateStudentQuestion(
      questionTitle,
      prevStatus,
      status,
      justification
    );

    cy.assertStudentQuestionEvaluation(
      questionTitle,
      status,
      justification === null ? '' : justification
    );
  });

  it('Approve a student question with justification', () => {
    let justification = 'Very good question, dear student!';
    let prevStatus = WAITING_FOR_APPROVAL;
    let status = APPROVED;

    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.evaluateStudentQuestion(
      questionTitle,
      prevStatus,
      status,
      justification
    );

    cy.assertStudentQuestionEvaluation(
      questionTitle,
      status,
      justification === null ? '' : justification
    );
  });

  it('Reject a student question with justification', () => {
    let justification = 'Very bad question, dear student!';
    let prevStatus = WAITING_FOR_APPROVAL;
    let status = REJECTED;

    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.evaluateStudentQuestion(
      questionTitle,
      prevStatus,
      status,
      justification
    );

    cy.assertStudentQuestionEvaluation(
      questionTitle,
      status,
      justification === null ? '' : justification
    );
  });

  it('Approve a rejected student question with justification', () => {
    let statuses = [WAITING_FOR_APPROVAL, REJECTED, APPROVED];
    let justifications = ['.', 'Miss click. Good question'];

    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="student-questions"]').click();

    for (let i = 0; i < statuses.length - 1; i += 1) {
      cy.evaluateStudentQuestion(
        questionTitle,
        statuses[i],
        statuses[i + 1],
        justifications[i]
      );

      cy.assertStudentQuestionEvaluation(
        questionTitle,
        statuses[i + 1],
        justifications[i]
      );
    }
  });

  // ===================================================================
  //  Invalid evaluations
  // ===================================================================
  it('Do not evaluate question', () => {
    let justification = null;
    let prevStatus = WAITING_FOR_APPROVAL;
    let status = WAITING_FOR_APPROVAL;

    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.evaluateStudentQuestion(
        questionTitle,
        prevStatus,
        status,
        justification
    );

    cy.errorMessageClose('You must approve or reject the question');

    cy.get('[data-cy="CancelEvaluation"]').click();
  });

  it('Reject a student question without justification', () => {
    let justification = null;
    let prevStatus = WAITING_FOR_APPROVAL;
    let status = REJECTED;

    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.evaluateStudentQuestion(
      questionTitle,
      prevStatus,
      status,
      justification
    );

    cy.errorMessageClose('Rejected questions must be justified');

    cy.get('[data-cy="CancelEvaluation"]').click();
  });

  it('Reject an approved student question with justification', () => {
    let status = [WAITING_FOR_APPROVAL, APPROVED, REJECTED];
    let justification = "Miss click. Horrible question :'(";

    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.evaluateStudentQuestion(
      questionTitle,
      status[0],
      status[1],
      null
    );
    cy.assertStudentQuestionEvaluation(
        questionTitle,
        status[1],
        ''
    );

    cy.evaluateStudentQuestion(
      questionTitle,
      status[1],
      status[2],
      justification
    );

    cy.errorMessageClose('Cannot reject already accepted suggestion');
    cy.get('[data-cy="CancelEvaluation"]').click();
  });
});
