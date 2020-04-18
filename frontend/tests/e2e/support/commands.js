// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })
/// <reference types="Cypress" />
Cypress.Commands.add('demoAdminLogin', () => {
  cy.visit('/');
  cy.get('[data-cy="adminButton"]').click();
  cy.contains('Administration').click();
  cy.contains('Manage Courses').click();
});

Cypress.Commands.add('demoTeacherLogin', () => {
  cy.visit('/');
  cy.get('[data-cy="teacherButton"]').click();
});

Cypress.Commands.add('demoStudentLogin', () => {
  cy.visit('/');
  cy.get('[data-cy="studentButton"]').click();
});

Cypress.Commands.add('demoTeacherLogin', () => {
  cy.visit('/');
  cy.get('[data-cy="teacherButton"]').click();
});

Cypress.Commands.add('logout', () => {
  // Work around VMenu bug
  // this handler runs at most once, and only matches a specific error
  cy.once('uncaught:exception', (error, _) => {
    if (error.message == "Cannot read property 'contains' of undefined") {
      return true;
    }

    return false;
  });

  cy.get('[data-cy="logout"]').click();
});

Cypress.Commands.add('createCourseExecution', (name, acronym, academicTerm) => {
  cy.get('[data-cy="createButton"]').click();
  cy.get('[data-cy="Name"]').type(name);
  cy.get('[data-cy="Acronym"]').type(acronym);
  cy.get('[data-cy="AcademicTerm"]').type(academicTerm);
  cy.get('[data-cy="saveButton"]').click();
});

Cypress.Commands.add('closeErrorMessage', (name, acronym, academicTerm) => {
  cy.contains('Error')
    .parent()
    .find('button')
    .click();
});

Cypress.Commands.add('deleteCourseExecution', acronym => {
  cy.contains(acronym)
    .parent()
    .should('have.length', 1)
    .children()
    .should('have.length', 7)
    .find('[data-cy="deleteCourse"]')
    .click();
});

Cypress.Commands.add(
  'createFromCourseExecution',
  (name, acronym, academicTerm) => {
    cy.contains(name)
      .parent()
      .should('have.length', 1)
      .children()
      .should('have.length', 7)
      .find('[data-cy="createFromCourse"]')
      .click();
    cy.get('[data-cy="Acronym"]').type(acronym);
    cy.get('[data-cy="AcademicTerm"]').type(academicTerm);
    cy.get('[data-cy="saveButton"]').click();
  }
);

/* STUDENT QUESTION TESTS */

Cypress.Commands.add('generateAndAnswerQuiz', () => {
  cy.get('[data-cy="quizzes"]').click();
  cy.contains('Create').click();
  cy.get('[data-cy="generate"]').click();
  cy.get('[data-cy="options"]')
    .first()
    .click();
  cy.get('[data-cy="options"]')
    .first()
    .click();
  cy.get('[data-cy="nextQuestion"]').click();
  cy.get('[data-cy="options"]')
    .first()
    .click();
  cy.get('[data-cy="nextQuestion"]').click();
  cy.get('[data-cy="options"]')
    .first()
    .click();
  cy.get('[data-cy="endQuiz"]').click();
  cy.get('[data-cy="sure"]').click();
});

Cypress.Commands.add('submitClarificationRequest', (content, n) => {
  if (n > 0) {
    for (i = 0; i < n; i++) {
      cy.get('[data-cy="newRequest"]').click();
      cy.get('[data-cy="inputRequest"]').type(content);
      cy.contains('Submit').click();
      let requests = cy.get('[data-cy="questionRequests"]').children();
      requests.should('have.length', 1);
      requests.first().should('have.text', content);
      cy.get('[data-cy="nextQuestion"]').click();
    }
  }
});

Cypress.Commands.add(
  'answerClarificationRequest',
  (requestText, answerText) => {
    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="teacherClarifications"]').click();
    cy.get(
      `[data-cy^="answerClarification-${requestText.slice(0, 15)}"]`
    ).click();

    cy.get('[data-cy="answerField"]').type(answerText);
    cy.get('[data-cy="answerSubmit"]').click();
  }
);

Cypress.Commands.add('deleteClarificationRequestAnswer', requestText => {
  cy.get('[data-cy="management"]').click();
  cy.get('[data-cy="teacherClarifications"]').click();
  cy.get(
    `[data-cy^="answerClarification-${requestText.slice(0, 15)}"]`
  ).click();

  cy.get('[data-cy="answerDelete"]').click();
});

Cypress.Commands.add(
  'createStudentQuestion',
  (title, content, topics, options, correctOptions) => {
    cy.get('[data-cy="NewQuestion"]').click();

    cy.get('[data-cy="Topics"]')
      .children()
      .find('form')
      .click();

    for (topic of topics) {
      cy.get('[data-cy="topicList"]')
        .children()
        .contains(topic)
        .click();
    }

    cy.get('[data-cy="Topics"]')
      .find('i')
      .click();

    if (title) cy.get('[data-cy="StudentQuestionTitle"]').type(title);
    if (content) cy.get('[data-cy="StudentQuestionContent"]').type(content);

    correctOptions.forEach(correctOption => {
      if (correctOption > 0 && correctOption < 5)
        cy.get(`[data-cy="CorrectOption${correctOption}"]`)
          .parent()
          .click();
    });

    for (let i = 1; i < options.length + 1; i++) {
      cy.get(`[data-cy  =Option${i}]`).type(options[i - 1]);
    }

    cy.get('[data-cy="SaveStudentQuestion"]').click();
  }
);

Cypress.Commands.add('errorMessageClose', message => {
  cy.contains(message)
    .parent()
    .find('button')
    .click();
});

Cypress.Commands.add(
  'editStudentQuestion',
  (
    command,
    questionTitle,
    newTitle,
    newContent,
    oldTopics,
    newTopics,
    newOptions
  ) => {
    if (command === 'edit') {
      cy.contains(questionTitle)
        .parent()
        .children()
        .contains('edit')
        .click();
    } else if (command === 'duplicate') {
      cy.contains(questionTitle)
        .parent()
        .children()
        .contains('cached')
        .click();
    }

    for (oldTopic of oldTopics) {
      cy.get(`[data-cy="${oldTopic}"]`)
        .find('button')
        .click();
    }

    cy.get('[data-cy="Topics"]')
      .children()
      .find('form')
      .click();

    for (newTopic of newTopics) {
      cy.get('[data-cy="topicList"]')
        .children()
        .contains(newTopic)
        .click();
    }

    cy.get('[data-cy="Topics"]')
      .find('i')
      .click();

    cy.get('[data-cy="StudentQuestionTitle"]')
      .clear()
      .type(newTitle);

    cy.get('[data-cy="StudentQuestionContent"]')
      .clear()
      .type(newContent);

    for (let i = 1; i < newOptions.length + 1; i++) {
      cy.get(`[data-cy=Option${i}]`)
        .clear()
        .type(newOptions[i - 1]);
    }

    cy.get('[data-cy="SaveStudentQuestion"]').click();
  }
);

Cypress.Commands.add(
  'evaluateStudentQuestion',
  (title, prevStatus, status, justification) => {
    // select evaluate question
    cy.contains(title)
      .parent()
      .contains(prevStatus)
      .click();

    // select drop down
    cy.get('.layout')
      .contains(prevStatus)
      .click();

    // select evaluation status
    cy.get('.v-list-item__content')
      .contains(status)
      .click();

    // write justification
    if (justification != null && justification != '') {
      cy.get('.v-textarea').type(justification);
    }

    // select evaluate button
    cy.get('button')
      .contains('Evaluate')
      .click();
  }
);

Cypress.Commands.add(
  'assertStudentQuestionEvaluation',
  (questionTitle, status, justification) => {
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
      .should('have.text', justification);
  }
);
