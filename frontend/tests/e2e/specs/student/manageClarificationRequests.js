describe('Clarification Request', () => {
  beforeEach(() => {
    cy.demoStudentLogin()
    
    cy.generateAndAnswerQuiz()
  })

  afterEach(() => {
    cy.contains('Logout').click()
  })

  it('login submits a clarification request', () => {
    cy.contains('Clarification Requests')
    cy.submitClarificationRequest('Test clarification content.', 1)
  });

  it('login submits two clarification requests', () => {
    cy.submitClarificationRequest('Test clarification content.', 2)
  });
});
