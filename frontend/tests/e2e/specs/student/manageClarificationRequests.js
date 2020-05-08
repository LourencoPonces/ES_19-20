describe('Clarification Request', () => {
  let content = 'Test clarification content.';
  let newContent = 'Edited request';
  let answer = '10/10 best request ever';

  beforeEach(() => {
    cy.demoStudentLogin();
    cy.generateAndAnswerQuiz();
  });

  afterEach(() => {
    cy.exec('psql tutordb < ../backend/resetGeneratedQuizzes.sql');
    cy.logout();
  });

  it('login submits and deletes a clarification request', () => {
    cy.contains('Clarification Requests');
    cy.submitClarificationRequest(content, 1);
    cy.goToMyClarifications();
    cy.deleteAllRequests(1);
  });

  it('login submits and deletes two clarification requests', () => {
    cy.submitClarificationRequest(content, 2);
    cy.goToMyClarifications();
    cy.deleteAllRequests(2);
  });


  it('login submit request with empty content', () => {
    cy.get('[data-cy="newRequest"]').click();
    cy.get('[data-cy="inputRequest"]').type('   ');
    cy.contains('Submit').click();
    cy.contains('Error')
      .should('contain.text', 'Missing content');
  });

  it('login and try and delete a request with more than one message', () => {
    cy.submitClarificationRequest(content, 1);
    cy.logout();

    cy.demoTeacherLogin();
    cy.answerClarificationRequest(content, answer);
    cy.logout();

    cy.demoStudentLogin();
    cy.goToMyClarifications();
    cy.get('[data-cy="delete"]')
      .should('be.disabled');
    cy.logout();

    cy.demoTeacherLogin();
    cy.deleteClarificationRequestMessage(content);
    cy.logout();

    cy.demoStudentLogin();
    cy.goToMyClarifications();
    cy.deleteAllRequests(1);
  });

  it('login and see request made public', () => {
    cy.submitClarificationRequest(content, 1);
    cy.goToMyClarifications();
    cy.get(`[data-cy^="private-${content.slice(0, 15)}"]`)
      .first()
      .should('exist');
    cy.logout();

    cy.demoTeacherLogin();
    cy.changeClarificationRequestStatus(content);
    cy.logout();

    cy.demoStudentLogin();
    cy.goToMyClarifications();
    cy.get(`[data-cy^="public-${content.slice(0, 15)}"]`)
      .first()
      .should('exist');

    cy.checkRequestInDiscussion(content);
    cy.get('[data-cy="newRequest"]')
      .should('be.disabled');

    cy.goToMyClarifications();
    cy.deleteAllRequests(1);
  });

});
