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

  beforeEach(() => {
    cy.demoTeacherLogin();
  });

  afterEach(() => {
    cy.logout();
  });

  it('can see unanswered clarification requests', () => {
    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="teacherUnansweredClarifications"]').click();
    cy.contains(TEST_REQ_1);
    cy.contains(TEST_REQ_2);
  });
});
