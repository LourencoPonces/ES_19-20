let APPROVED = 'Approved';
let REJECTED = 'Rejected';
let WAITING_FOR_APPROVAL = 'Waiting for Approval';
let PROMOTED = 'Promoted';

describe('Student Question Promotion', () => {
  let questionTitle;
  let newQuestionTitle;
  let questionContent = 'To be or not to be?';
  let topics = ['Adventure Builder'];
  let options = ['AAAA', 'BBBB', 'CCCC', 'DDDD'];

  beforeEach(() => {
    questionTitle = 'Question #' + Date.now().toString();
    newQuestionTitle = 'New ' + questionTitle;
    // log in as student
    cy.demoStudentLogin();

    // create student question
    cy.get('[data-cy="my-area"]').click();
    cy.get('[data-cy="student-questions"]').click();
    cy.createStudentQuestion(questionTitle, questionContent, topics, options, [
      1
    ]);

    cy.logout();

    // log in as teacher
    cy.demoTeacherLogin();
  });

  afterEach(() => {
    cy.logout();
  });

  it('Promote a student question with justification', () => {
      let justification = 'Excellent question, dear student!';
      let prevStatus = WAITING_FOR_APPROVAL;
      let status = PROMOTED;

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

      cy.assertQuestionExists(questionTitle);
  });

  it('Promote student question and fail to reevaluate it', () => {
      let justification = 'Very good question, dear student!';
      let prevStatus = WAITING_FOR_APPROVAL;
      let status = PROMOTED;

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

      // select evaluate question
      cy.contains(questionTitle)
          .parents('tr')
          .eq(0)
          .contains(status)
          .click();

      cy.get('[data-cy="do-evaluate"]').should('not.exist');
  });

  it('Edit and promote student question', () => {
      let newContent = 'New content';
      let status = PROMOTED;
      let justification = 'Question edited by teacher';
      cy.get('[data-cy="management"]').click();
      cy.get('[data-cy="student-questions"]').click();

      // (questionTitle, newTitle, newContent, oldTopics, newTopics, newOptions)
      cy.editAndPromoteStudentQuestion(
          questionTitle,
          newQuestionTitle,
          newContent,
          topics,
          topics,
          options
      );

      cy.assertStudentQuestionEvaluation(
          newQuestionTitle,
          status,
          justification === null ? '' : justification
      );

      cy.assertQuestionExists(newQuestionTitle);
  });

  it('Edit and fail to promote invalid student question', () => {
    const newContent = '';
    const status = PROMOTED;
    const justification = 'Question edited by teacher';
    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="student-questions"]').click();

    // (questionTitle, newTitle, newContent, oldTopics, newTopics, newOptions)
    cy.editAndPromoteStudentQuestion(
      questionTitle,
      newQuestionTitle,
      newContent,
      topics,
      topics,
      options
    );

    cy.errorMessageClose(
      'Question must have title, content, and at least one topic'
    );
  });
});
