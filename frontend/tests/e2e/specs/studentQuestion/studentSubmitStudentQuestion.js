describe('Student Question Submission', () => {
  let questionTitle = 'Test Question Title';
  let questionContent = 'Test Question Content';
  let topics = ['Adventure Builder', 'Chrome'];
  let topic = ['GitHub'];
  let options = [
    'Test Option 1',
    'Test Option 2',
    'Test Option 3',
    'Test Option 4'
  ];

  beforeEach(() => {
    cy.demoStudentLogin();
  });

  afterEach(() => {
    cy.contains('Logout').click();
  });

  it('login and creates a new Student Question', () => {
    cy.contains('My Area').click();
    cy.contains('Questions').click();
    cy.createStudentQuestion(questionTitle, questionContent, topics, options, 1);
    cy.contains(questionTitle)
      .parent()
      .children()
      .should('have.length', 7);
    cy.get('[data-cy=questionTopics').should('have.length', topics.length);
    cy.get('[data-cy="showStatus"]').should(
      'have.text',
      'Waiting for Approval'
    );
    cy.get('[data-cy="deleteStudentQuestion"]').click();
  });

  it('login and create invalid questions'), () => {
    cy.contains('My Area').click();
    cy.contains('Questions').click();
    cy.createNoTitleStudentQuestion(questionContent, topic, options, 1);
    cy.createNoContentStudentQuestion(questionTitle, topic, options, 1);
    cy.createNoTopicsStudentQuestion(questionTitle, questionContent, options, 1);
    cy.createNoOptionsStudentQuestion(questionTitle, questionContent, topic, 1);
    cy.createNoCorrectOptionStudentQuestion(questionTitle, questionContent, topic, options);
    cy.get('[data-cy="deleteStudentQuestion"]').click();
  }

});
