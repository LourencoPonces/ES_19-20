describe('Teacher Clarification Requests', () => {
  const TEST_REQ_1 = 'TEST_REQ_1';
  const TEST_REQ_2 = 'TEST_REQ_2';
  const TEST_REQ_3 = 'TEST_REQ_3';

  before(() => {
    cy.demoStudentLogin();
    cy.generateAndAnswerQuiz();
    cy.submitClarificationRequest(TEST_REQ_1, 1);
    cy.submitClarificationRequest(TEST_REQ_2, 1);
    cy.submitClarificationRequest(TEST_REQ_3, 1);
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

  it('can answer requests', () => {
    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="teacherClarifications"]').click();
    cy.contains(TEST_REQ_1);
    cy.contains(TEST_REQ_2);
    cy.contains(TEST_REQ_3);

    cy.answerClarificationRequest(TEST_REQ_3, '10/10 best request ever');
  });
});
