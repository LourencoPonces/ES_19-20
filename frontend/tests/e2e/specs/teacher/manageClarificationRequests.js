describe('Teacher Clarification Requests', () => {
  const TEST_REQ_1 = 'TEST_REQ_1';
  const TEST_REQ_2 = 'TEST_REQ_2';

  const TEST_MSG_1 = '10/10 best request ever';
  const TEST_MSG_2 = '5/7 approved';

  before(() => {
    cy.demoStudentLogin();
    cy.generateAndAnswerQuiz();
    cy.submitClarificationRequest(TEST_REQ_1, 1);
    cy.submitClarificationRequest(TEST_REQ_2, 1);
    cy.goToMyClarificationsStudent();
    cy.expandClarificationRequest(TEST_REQ_1);
    cy.messageClarificationRequest(TEST_MSG_1, false);
    cy.wait(1000);
    cy.logout();
  });

  after(() => {
    cy.demoStudentLogin();
    cy.goToMyClarificationsStudent();
    cy.expandClarificationRequest(TEST_REQ_1);
    cy.deleteClarificationMessage(TEST_MSG_1);
    cy.deleteClarificationRequest(TEST_REQ_1);
    cy.deleteClarificationRequest(TEST_REQ_2);
    cy.wait(1000);
    cy.logout();
    cy.exec('psql tutordb < ../backend/resetGeneratedQuizzes.sql');
  });

  beforeEach(() => {
    cy.demoTeacherLogin();
  });

  afterEach(() => {
    cy.logout();
  });

  it('can see clarification requests, and see, add and delete messages', () => {
    cy.goToMyClarificationsTeacher();
    cy.contains(TEST_REQ_1);
    cy.contains(TEST_REQ_2);

    cy.expandClarificationRequest(TEST_REQ_1);

    // student message is visible
    cy.contains(TEST_MSG_1);

    // we can add our own
    cy.messageClarificationRequest(TEST_MSG_2, true);
    cy.wait(1000);
    cy.contains(TEST_MSG_2);

    cy.deleteClarificationMessage(TEST_MSG_2);
  });

  it('can make clarication request public', () => {
    cy.goToMyClarificationsTeacher();
    cy.changeClarificationRequestStatus(TEST_REQ_2);
  });
});
