let APPROVED = 'Approved';
let REJECTED = 'Rejected';
let WAITING_FOR_APPROVAL = 'Waiting for Approval';

describe('Student Question Submission', () => {
  let ts;
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
    ts = Date.now().toString();
    questionTitle = 'Question #' + ts;
    newQuestionTitle = 'New Question #' + ts;
    cy.demoStudentLogin();
  });

  afterEach(() => {
    // clean exit
    cy.contains('Demo Course').click();
    cy.logout();
  });

  // Test 1
  it('Create a valid Student Question', () => {
    cy.get('[data-cy="my-area"]').click();
    cy.get('[data-cy="student-questions"]').click();

    // Create the question
    cy.createStudentQuestion(
      questionTitle,
      questionContent,
      topics,
      options,
      1
    );

    // verifications
    cy.contains(questionTitle)
      .parent()
      .children()
      .should('have.length', 7);

    for (topictoAdd of topics)
      cy.get('[data-cy=questionTopics')
        .children()
        .contains(topictoAdd);

    cy.contains(questionTitle)
      .parent()
      .children()
      .eq(3)
      .should('have.text', WAITING_FOR_APPROVAL);

    // delete created question
    cy.contains(questionTitle)
      .parent()
      .within(() => {
        cy.get('[data-cy="deleteStudentQuestion"]').click();
      });
  });

  // Test 2
  it('login and create invalid questions', () => {
    cy.get('[data-cy="my-area"]').click();
    cy.get('[data-cy="student-questions"]').click();

    // Creation of bad questions
    // No Title
    cy.createNoTitleStudentQuestion(questionContent, topic, options, 1);

    // No Content
    cy.createNoContentStudentQuestion(questionTitle, topic, options, 1);

    // No Topics
    cy.createNoTopicsStudentQuestion(
      questionTitle,
      questionContent,
      options,
      1
    );

    // No Options
    cy.createNoOptionsStudentQuestion(questionTitle, questionContent, topic, 1);

    // No Correct Option
    cy.createNoCorrectOptionStudentQuestion(
      questionTitle,
      questionContent,
      topic,
      options
    );
  });

  // Test 3
  it('login and edit question', () => {
    cy.get('[data-cy="my-area"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.createStudentQuestion(
      questionTitle,
      questionContent,
      topics,
      options,
      1
    );

    cy.editStudentQuestion(
      'edit',
      questionTitle,
      newQuestionTitle,
      newQuestionContent,
      topics,
      newTopics,
      newOptions
    );

    // verifications
    cy.contains(newQuestionTitle)
      .parent()
      .children()
      .should('have.length', 7);

    for (let newTopic of newTopics) {
      cy.get('[data-cy=questionTopics')
        .children()
        .contains(newTopic);
    }

    cy.contains(questionTitle)
      .parent()
      .children()
      .eq(3)
      .should('have.text', WAITING_FOR_APPROVAL);

    // delete the question
    cy.contains(newQuestionTitle)
      .parent()
      .within(() => {
        cy.get('[data-cy="deleteStudentQuestion"]').click();
      });
  });

  // Test 4
  it('login and duplicate question', () => {
    cy.get('[data-cy="my-area"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.createStudentQuestion(
      questionTitle,
      questionContent,
      topics,
      options,
      1
    );

    cy.editStudentQuestion(
      'duplicate',
      questionTitle,
      newQuestionTitle,
      newQuestionContent,
      topics,
      newTopics,
      newOptions
    );

    // verification and deletion
    cy.contains(questionTitle)
      .parent()
      .children()
      .contains('delete')
      .click();

    cy.contains(newQuestionTitle)
      .parent()
      .children()
      .contains('delete')
      .click();
  });
});
