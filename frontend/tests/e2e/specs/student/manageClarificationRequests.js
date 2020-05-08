describe('Clarification Request', () => {
  const REQ_1 = '1Test clarification content.';
  const REQ_2 = '2Test clarification content.';
  const MSG = '10/10 best request ever';

  beforeEach(() => {
    cy.demoStudentLogin();
    cy.generateAndAnswerQuiz();
  });

  afterEach(() => {
    cy.exec('psql tutordb < ../backend/resetGeneratedQuizzes.sql');
    cy.logout();
  });

  it('can submit and delete two clarification requests', () => {
    cy.submitClarificationRequest(REQ_1, 1);
    cy.submitClarificationRequest(REQ_2, 1);
    cy.goToMyClarificationsStudent();
    cy.deleteClarificationRequest(REQ_1);
    cy.deleteClarificationRequest(REQ_2);
  });

  it("can't submit request with empty content", () => {
    cy.get('[data-cy="newRequest"]').click();
    cy.get('[data-cy="inputRequest"]').type('   ');
    cy.contains('Submit').click();
    cy.contains('Error').should('contain.text', 'Missing content');
  });

  it("can't delete a request with messages", () => {
    cy.submitClarificationRequest(REQ_1, 1);

    cy.goToMyClarificationsStudent();
    cy.expandClarificationRequest(REQ_1);
    cy.messageClarificationRequest(MSG, false);

    cy.get('[data-cy="deleteRequest"]').should('be.disabled');

    cy.deleteClarificationMessage(MSG);
    cy.deleteClarificationRequest(REQ_1);
  });

  it('can see request made public', () => {
    cy.submitClarificationRequest(REQ_1, 1);
    cy.goToMyClarificationsStudent();
    cy.get(`[data-cy^="private-${REQ_1.slice(0, 15)}"]`)
      .first()
      .should('exist');
    cy.logout();

    cy.demoTeacherLogin();
    cy.goToMyClarificationsTeacher();
    cy.changeClarificationRequestStatus(REQ_1);
    cy.logout();

    cy.demoStudentLogin();
    cy.goToMyClarificationsStudent();
    cy.get(`[data-cy^="public-${REQ_1.slice(0, 15)}"]`)
      .first()
      .should('exist');

    cy.checkRequestInDiscussion(REQ_1);
    cy.get('[data-cy="newRequest"]').should('be.disabled');

    cy.goToMyClarificationsStudent();
    cy.deleteClarificationRequest(REQ_1);
  });
});
