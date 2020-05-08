describe('Clarification Request', () => {
  const content = 'Test clarification content.';
  const demoStudent = 'Demo Student';
  const PRIVATE = 'Private';
  
  beforeEach(() => {
    cy.demoStudentLogin();
  });

  afterEach(() => {
    // clean exit
    cy.exec('psql tutordb < ../backend/resetGeneratedQuizzes.sql');
    cy.contains('Demo Course').click();
    cy.logout();
  });

  it('change demo student dashboard stats visibility', () => {
    // make the 2 stats private and check
    cy.get('[data-cy="dashboardButton"]').click();
    const requestsSubmittedDiv = '[data-cy="requestsSubmittedDiv"]';
    const publicRequestsDiv = '[data-cy="publicRequestsDiv"]';
    const requestsSubmittedRow = '[data-cy="requestsSubmittedRow"]';
    const publicRequestsRow = '[data-cy="publicRequestsRow"]';

    cy.makePrivate(requestsSubmittedDiv);
    cy.makePrivate(publicRequestsDiv);

    cy.openDashboardStatsDialog(demoStudent);
    cy.checkVisibility(requestsSubmittedRow, PRIVATE);
    cy.checkVisibility(publicRequestsRow, PRIVATE);
    cy.get('[data-cy="closeDashboardTable"]').click();

    // make the 2 stats public and check
    cy.makePublic(requestsSubmittedDiv);
    cy.makePublic(publicRequestsDiv);

    cy.openDashboardStatsDialog(demoStudent);
    cy.checkVisibility(requestsSubmittedRow, '0');
    cy.checkVisibility(publicRequestsRow, '0');
    cy.get('[data-cy="closeDashboardTable"]').click();
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
