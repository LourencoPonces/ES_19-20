describe('Teacher Clarification Requests', () => {
  const TEST_REQ_1 = 'TEST_REQ_1';
  const TEST_REQ_2 = 'TEST_REQ_2';

  before(() => {
    cy.demoStudentLogin();
    cy.generateAndAnswerQuiz();
    cy.submitClarificationRequest(TEST_REQ_1, 1);
    cy.submitClarificationRequest(TEST_REQ_2, 1);
    cy.logout();
  });

  after(() => {
    cy.demoStudentLogin();
    cy.goToMyClarifications();
    cy.deleteAllRequests(2);
    cy.logout();
  });


  beforeEach(() => {
    cy.demoTeacherLogin();
  });

  afterEach(() => {
    cy.logout();
  });

  it('can see clarification requests', () => {
    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="teacherClarifications"]').click();
    cy.contains(TEST_REQ_1);
    cy.contains(TEST_REQ_2);
  });

  it('can answer requests, update the answer and delete it', () => {
    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="teacherClarifications"]').click();
    cy.contains(TEST_REQ_1);
    cy.contains(TEST_REQ_2);

    cy.answerClarificationRequest(TEST_REQ_2, '10/10 best request ever');

    cy.answerClarificationRequest(TEST_REQ_2, '5/7 i changed my mind')

    cy.deleteClarificationRequestAnswer(TEST_REQ_2);
  });

  it('make clarication request public', () => {
    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="teacherClarifications"]').click();
    cy.get(`[data-cy^="changeStatus-${TEST_REQ_2.slice(0, 15)}"]`)
      .first()
      .click()
  });
});
