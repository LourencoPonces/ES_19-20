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
    cy.createStudentQuestion(questionTitle, questionContent, topics, options, [
      1
    ]);

    for (topictoAdd of topics)
      cy.contains(questionTitle) // get question title
        .parents('tr') // get question row
        .eq(0) // make sure closest row
        .find('[data-cy=questionTopics]')
        .children()
        .contains(topictoAdd);

    cy.contains(questionTitle)
      .parents('tr')
      .eq(0)
      .find('[data-cy=showStatus]')
      .should('have.text', WAITING_FOR_APPROVAL);

    // delete created question
    cy.contains(questionTitle)
      .parents('tr')
      .eq(0)
      .find('[data-cy="deleteStudentQuestion"]')
      .click();
  });

  // Test 2
  it('Fail to create an invalid question', () => {
    cy.get('[data-cy="my-area"]').click();
    cy.get('[data-cy="student-questions"]').click();

    // No title
    cy.createStudentQuestion('', questionContent, topic, options, [1]);
    cy.errorMessageClose(
      'Question must have title, content, and at least one topic'
    );
    cy.get('[data-cy="CancelStudentQuestion"]').click();

    // no Content
    cy.createStudentQuestion(questionTitle, '', topic, options, [1]);
    cy.errorMessageClose(
      'Question must have title, content, and at least one topic'
    );
    cy.get('[data-cy="CancelStudentQuestion"]').click();

    // no topics
    cy.createStudentQuestion(questionTitle, questionContent, [], options, [1]);
    cy.errorMessageClose('Error: The question has no Topics');
    cy.get('[data-cy="CancelStudentQuestion"]').click();

    // no options
    cy.createStudentQuestion(questionTitle, questionContent, topic, [], [1]);
    cy.errorMessageClose('Error: Missing information for question');
    cy.get('[data-cy="CancelStudentQuestion"]').click();

    // no correct option
    cy.createStudentQuestion(questionTitle, questionContent, topic, options, [
      0
    ]);
    cy.errorMessageClose(
      "Error: The question doesn't have any correct options"
    );
    cy.get('[data-cy="CancelStudentQuestion"]').click();

    // several correct options
    cy.createStudentQuestion(questionTitle, questionContent, topic, options, [
      1,
      3
    ]);
    cy.errorMessageClose('Error: Questions can only have 1 correct option');
    cy.get('[data-cy="CancelStudentQuestion"]').click();
  });

  // Test 3
  it('Edit an existing student question', () => {
    cy.get('[data-cy="my-area"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.createStudentQuestion(questionTitle, questionContent, topics, options, [
      1
    ]);

    cy.editStudentQuestion(
      questionTitle,
      newQuestionTitle,
      newQuestionContent,
      topics,
      newTopics,
      newOptions
    );

    for (topictoAdd of topics)
      cy.contains(questionTitle) // get question title
        .parents('tr') // get question row
        .eq(0) // make sure closest row
        .find('[data-cy=questionTopics]')
        .children()
        .contains(topictoAdd);

    cy.contains(questionTitle)
      .parents('tr')
      .eq(0)
      .find('[data-cy=showStatus]')
      .should('have.text', WAITING_FOR_APPROVAL);

    // delete the question
    cy.contains(questionTitle)
      .parents('tr')
      .eq(0)
      .find('[data-cy="deleteStudentQuestion"]')
      .click();
  });

  // Test 4
  it('Edit a rejected student question', () => {
    let justification = 'Very bad question, dear student!';
    let prevStatus = WAITING_FOR_APPROVAL;
    let status = REJECTED;

    cy.get('[data-cy="my-area"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.createStudentQuestion(questionTitle, questionContent, topics, options, [
      1
    ]);

    // Teacher rejects questions
    cy.contains('Demo Course').click();
    cy.logout();
    cy.demoTeacherLogin();

    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.evaluateStudentQuestion(
      questionTitle,
      prevStatus,
      status,
      justification
    );
    // Student edits it
    cy.contains('Demo Course').click();
    cy.logout();
    cy.demoStudentLogin();

    cy.get('[data-cy="my-area"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.editStudentQuestion(
      questionTitle,
      newQuestionTitle,
      newQuestionContent,
      topics,
      newTopics,
      newOptions
    );

    for (topictoAdd of newTopics)
      cy.contains(questionTitle) // get question title
        .parents('tr') // get question row
        .eq(0) // make sure closest row
        .find('[data-cy=questionTopics]')
        .children()
        .contains(topictoAdd);

    cy.contains(newQuestionTitle)
      .parents('tr')
      .eq(0)
      .find('[data-cy=showStatus]')
      .should('have.text', WAITING_FOR_APPROVAL);

    // delete the question
    cy.contains(questionTitle)
      .parents('tr')
      .eq(0)
      .find('[data-cy="deleteStudentQuestion"]')
      .click();
  });

  // Test 5
  it('Duplicate an existing student question' + '', () => {
    cy.get('[data-cy="my-area"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.createStudentQuestion(questionTitle, questionContent, topics, options, [
      1
    ]);

    cy.duplicateStudentQuestion(
      questionTitle,
      newQuestionTitle,
      newQuestionContent,
      topics,
      newTopics,
      newOptions
    );

    // delete the question
    cy.contains(questionTitle)
      .parents('tr')
      .eq(0)
      .find('[data-cy="deleteStudentQuestion"]')
      .click();

    cy.contains(newQuestionTitle)
      .parents('tr')
      .eq(0)
      .find('[data-cy="deleteStudentQuestion"]')
      .click();
  });
});
