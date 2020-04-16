describe('Student Question Evaluation', () => {
  let questionTitle = 'Question #' + Date.now().toString();
  let questionContent = 'To be or not to be?';
  let topics = ['Adventure Builder'];
  let options = ['AAAA', 'BBBB', 'CCCC', 'DDDD'];

  beforeEach(() => {
    // log in as student
    cy.demoStudentLogin();

    // create student question
    cy.get('[data-cy="my-area"]').click();
    cy.get('[data-cy="student-questions"]').click();
    cy.createStudentQuestion(
      questionTitle,
      questionContent,
      topics,
      options,
      1
    );

    // logout
    cy.contains('Logout').click();

    // log in as teacher
    cy.demoTeacherLogin();
  });

  afterEach(() => {
    cy.contains('Logout').click();
  });

  // Test 1
  it('Approve a student question without justification', () => {
    let justification = null;
    let status = 'Approved';

    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.evaluateStudentQuestion(questionTitle, status, justification);

    // assert status
    cy.contains(questionTitle)
      .parent()
      .children()
      .eq(3)
      .should('have.text', status);

    // assert justification
    cy.contains(questionTitle)
        .parent()
        .children()
        .eq(4)
        .should('have.text', justification === null ? '' : justification);
  });
});
