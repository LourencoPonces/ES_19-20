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

Cypress.Commands.add('demoStudentLogin', () => {
  cy.visit('/');
  cy.get('[data-cy="studentButton"]').click();
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
  'createStudentQuestion',
  (title, content, topics, options, correctOption) => {
    cy.get('[data-cy="NewQuestion"]').click();

    // wait for dialog to open
    cy.wait(10);

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

    if (correctOption > 0 && correctOption < 5)
      cy.get(`[data-cy="CorrectOption${correctOption}"]`)
        .parent()
        .click();

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
  'createNoTitleStudentQuestion',
  (content, topic, options, correctOption) => {
    cy.createStudentQuestion('', content, topic, options, correctOption);

    cy.errorMessageClose(
      'Question must have title, content, and at least one topic'
    );

    cy.get('[data-cy="CancelStudentQuestion"]').click();
  }
);

Cypress.Commands.add(
  'createNoContentStudentQuestion',
  (title, topic, options, correctOption) => {
    cy.createStudentQuestion(title, '', topic, options, correctOption);

    cy.errorMessageClose(
      'Question must have title, content, and at least one topic'
    );

    cy.get('[data-cy="CancelStudentQuestion"]').click();
  }
);

Cypress.Commands.add(
  'createNoTopicsStudentQuestion',
  (title, content, options, correctOption) => {
    cy.createStudentQuestion(title, content, [], options, correctOption);

    cy.errorMessageClose('Error: The question has no Topics');

    cy.get('[data-cy="CancelStudentQuestion"]').click();
  }
);

Cypress.Commands.add(
  'createNoOptionsStudentQuestion',
  (title, content, topic, correctOption) => {
    cy.createStudentQuestion(title, content, topic, [], correctOption);

    cy.errorMessageClose('Error: Missing information for question');

    cy.get('[data-cy="CancelStudentQuestion"]').click();
  }
);

Cypress.Commands.add(
  'createNoCorrectOptionStudentQuestion',
  (title, content, topic, options) => {
    cy.createStudentQuestion(title, content, topic, options, 0);

    cy.errorMessageClose(
      // eslint-disable-next-line prettier/prettier
      'Error: The question doesn\'t have any correct options'
    );

    cy.get('[data-cy="CancelStudentQuestion"]').click();
  }
);

Cypress.Commands.add(
  'editStudentQuestion',
  (command, newTitle, newContent, oldTopics, newTopics, newOptions) => {
    if (command === 'edit') cy.get('[data-cy="editStudentQuestion"]').click();
    else if (command === 'duplicate')
      cy.get('[data-cy="duplicateStudentQuestion"]').click();

    cy.wait(10);

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
  'createTournament',
  (title, numberOfQuestions, includeAvailable, dateOrder) => {
    let availableNr = 1;
    let runningNr = 2;
    let conclusionNr = 3;

    if (dateOrder) {
      let nr = 0;
      dateOrder.forEach(date => {
        nr++;
        switch (date) {
          case 'available':
            availableNr = nr;
            break;
          case 'running':
            runningNr = nr;
            break;
          case 'conclusion':
            conclusionNr = nr;
            break;
        }
      });
    }

    cy.get('[data-cy="newTournament"]').click({ force: true });

    // wait for dialog to open
    cy.wait(500);

    cy.get('[data-cy="title"]').type(title);

    cy.get('[data-cy="numberOfQuestions"').type('12');

    if (includeAvailable) {
      cy.contains('.v-label', 'Available Date').click({ force: true });
      // Always click for the next month. The chosen days are guaranteed to work
      // and won't collide with the current day.
      cy.get('.mdi-chevron-right').click({ multiple: true, force: true });
      // select day
      cy.get(
        `.v-date-picker-table > table > tbody > :nth-child(3) > :nth-child(${availableNr}) > .v-btn`
      ).click({ multiple: true, force: true });
      cy.contains('OK').click();
    }

    cy.contains('.v-label', 'Running Date').click({ force: true });
    cy.get('.mdi-chevron-right').click({ multiple: true, force: true });
    // select day + 1
    // The previously opened date pickers still exist, even though they aren't visible.
    // The most recently opened one is the last in the list.
    cy.get(
      `.v-date-picker-table > table > tbody > :nth-child(3) > :nth-child(${runningNr}) > .v-btn`
    )
      .last()
      .click({ force: true });
    // click ok, contains('OK') doesn't work...
    cy.get('.v-card__actions > .green--text > .v-btn__content').click({
      multiple: true,
      force: true
    });

    cy.contains('.v-label', 'Conclusion Date').click({ force: true });
    cy.get('.mdi-chevron-right').click({ multiple: true, force: true });
    // select day + 2
    cy.get(
      `.v-date-picker-table > table > tbody > :nth-child(3) > :nth-child(${conclusionNr}) > .v-btn`
    )
      .last()
      .click({ force: true });
    // click ok
    cy.get('.v-card__actions > .green--text > .v-btn__content').click({
      multiple: true,
      force: true
    });

    cy.get('[data-cy="topics"').click();
    cy.get('[role=listbox]')
      .children()
      .first()
      .click({ force: true });

    cy.get('[data-cy="saveTournament"]').click();
  });
