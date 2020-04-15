describe('Clarification Request', () => {
  let content = 'Test clarification content.' 
  let newContent = 'Edited request'

  beforeEach(() => {
    cy.demoStudentLogin()
    
    cy.generateAndAnswerQuiz()
  });

  afterEach(() => {
    cy.contains('Logout').click()
  });

  it('login submits a clarification request', () => {
    cy.contains('Clarification Requests')
    cy.submitClarificationRequest(content, 1)
    cy.goToMyClarifications()
    cy.deleteAllRequests(1, content)
  });

  it('login submits two clarification requests', () => {
    cy.submitClarificationRequest(content, 2)
    cy.goToMyClarifications()
    cy.deleteAllRequests(2, content)
  });

  it('login submits, edits and deletes a clarification request', () => {
    cy.contains('Clarification Requests')
    cy.submitClarificationRequest(content, 1)
    cy.goToMyClarifications()
    cy.editClarificationRequest(newContent)
    cy.deleteAllRequests(1, newContent)
  });
});
