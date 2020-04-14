describe('Student Question Submission', () => {
  let questionTitle = 'Test Question Title';
  let newQuestionTitle = 'New Test Question Title';
  let questionContent = 'Test Question Content';
  let newQuestionContent = 'New Test Question Content';
  let topics = ['Adventure Builder', 'Chrome'];
  let newTopics = ['Hadoop', 'Infinispan'];
  let topic = ['GitHub'];
  let options = [
    'Test Option 1',
    'Test Option 2',
    'Test Option 3',
    'Test Option 4'
  ];
  let newOptions = [
    'New Test Option 1',
    'New Test Option 2',
    'New Test Option 3',
    'New Test Option 4'
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
    cy.createStudentQuestion(
      questionTitle,
      questionContent,
      topics,
      options,
      1
    );
    cy.contains(questionTitle)
      .parent()
      .children()
      .should('have.length', 7);
    for (let i = 0; i < topics.length; i++)
      cy.get('[data-cy=questionTopics')
        .children()
        .contains(`${topics[i]}`);
    cy.get('[data-cy="showStatus"]').should(
      'have.text',
      'Waiting for Approval'
    );
    cy.contains(questionTitle)
      .parent()
      .within(() => {
        cy.get('[data-cy="deleteStudentQuestion"]').click();
      });
  });

  it('login and create invalid questions', () => {
    cy.contains('My Area').click();
    cy.contains('Questions').click();
    cy.createNoTitleStudentQuestion(questionContent, topic, options, 1);
    cy.createNoContentStudentQuestion(questionTitle, topic, options, 1);
    cy.createNoTopicsStudentQuestion(
      questionTitle,
      questionContent,
      options,
      1
    );
    cy.createNoOptionsStudentQuestion(questionTitle, questionContent, topic, 1);
    cy.createNoCorrectOptionStudentQuestion(
      questionTitle,
      questionContent,
      topic,
      options
    );
  });

  it('login and edit question', () => {
    cy.contains('My Area').click();
    cy.contains('Questions').click();
    cy.createStudentQuestion(
      questionTitle,
      questionContent,
      topics,
      options,
      1
    );
    cy.editStudentQuestion(
      'edit',
      newQuestionTitle,
      newQuestionContent,
      topics,
      newTopics,
      newOptions
    );
    cy.contains(newQuestionTitle)
      .parent()
      .children()
      .should('have.length', 7);
    for (let i = 0; i < newTopics.length; i++)
      cy.get('[data-cy=questionTopics')
        .children()
        .contains(`${newTopics[i]}`);
    cy.get('[data-cy="showStatus"]').should(
      'have.text',
      'Waiting for Approval'
    );
    cy.contains(newQuestionTitle)
      .parent()
      .within(() => {
        cy.get('[data-cy="deleteStudentQuestion"]').click();
      });
  });

  it('login and duplicate question', () => {
    cy.contains('My Area').click();
    cy.contains('Questions').click();
    cy.createStudentQuestion(
      questionTitle,
      questionContent,
      topics,
      options,
      1
    );
    cy.editStudentQuestion(
      'duplicate',
      newQuestionTitle,
      newQuestionContent,
      topics,
      newTopics,
      newOptions
    );
    cy.get('[data-cy="studentQuestionTable"]')
      .find('tbody')
      .children()
      .should('have.length', 2)
      .first()
      .get('td')
      .should('have.html', newQuestionTitle);
    cy.contains(newQuestionTitle)
      .parent()
      .within(() => {
        cy.get('[data-cy="deleteStudentQuestion"]').click();
      });
    cy.wait(1000);
    cy.contains(questionTitle)
      .parent()
      .within(() => {
        cy.get('[data-cy="deleteStudentQuestion"]').click();
      });
  });
});
