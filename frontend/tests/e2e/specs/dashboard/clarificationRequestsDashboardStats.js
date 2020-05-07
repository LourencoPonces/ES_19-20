describe('Clarification Request', () => {
  let content = 'Test clarification content.';
  
  beforeEach(() => {
    cy.demoStudentLogin();
  });

  afterEach(() => {
    // clean exit
    cy.exec('psql tutordb < ../backend/resetGeneratedQuizzes.sql');
    cy.contains('Demo Course').click();
    cy.logout();
  });

  it('check no public and submitted requests', () => {
    cy.get('[data-cy="dashboardButton"]').click();
    cy.get('[data-cy="requestsSubmitted"]').contains('0');
    cy.get('[data-cy="publicRequests"]').contains('0');
  });

  it('submit 2 requests, teacher makes one public, reflected in dashboard', () => {
    cy.generateAndAnswerQuiz();
    cy.submitClarificationRequest(content, 2);

    cy.logout()
    cy.demoTeacherLogin();
    cy.changeClarificationRequestStatus(content);
    cy.logout();

    cy.demoStudentLogin();
    cy.get('[data-cy="dashboardButton"]').click();
    cy.get('[data-cy="requestsSubmitted"]').contains('2');
    cy.get('[data-cy="publicRequests"]').contains('1');

    cy.goToMyClarifications();
    cy.deleteAllRequests(2);
  });
});
