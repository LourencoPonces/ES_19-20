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

<<<<<<< HEAD
Cypress.Commands.add('demoStudentLogin', () => {
  cy.visit('/');
  cy.get('[data-cy="studentButton"]').click();
});
=======
Cypress.Commands.add('demoStudentLogin',  () => {
  cy.visit('/')
  cy.get('[data-cy="studentButton"]').click()
})
>>>>>>> PpA

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
  cy.get('[data-cy="quizzes"]').click()
  cy.contains('Create').click()
  cy.get('[data-cy="generate"]').click()
  cy.get('[data-cy="options"]')
      .first()
      .click()
  cy.get('[data-cy="options"]')
      .first()
      .click()
  cy.get('[data-cy="nextQuestion"]').click()
  cy.get('[data-cy="options"]')
      .first()
      .click()
  cy.get('[data-cy="nextQuestion"]').click()
  cy.get('[data-cy="options"]')
      .first()
      .click()
  cy.get('[data-cy="endQuiz"]').click()
  cy.get('[data-cy="sure"]').click()
});

Cypress.Commands.add('submitClarificationRequest', (content, n) => {
  if (n > 0) {
      for (i = 0; i < n; i++) {
          cy.get('[data-cy="newRequest"]').click()
          cy.get('[data-cy="inputRequest"]').type(content)
          cy.contains('Submit').click()
          let requests = cy.get('[data-cy="questionRequests"]')
                            .children()
          requests.should('have.length', 1)
          requests.first()
                  .should('have.text', content)
          cy.get('[data-cy="nextQuestion"]').click()
      }
  }
});

Cypress.Commands.add(
  'createStudentQuestion',
  (title, content, topics, options, correctOption) => {
    cy.get('[data-cy="NewQuestion"]').click();
    cy.wait(10);
    cy.get('[data-cy="Topics"]')
      .children()
      .find('form')
      .click();
    for (topic in topics) {
      cy.get('[data-cy="topicList"]')
        .children()
        .contains(`${topic}`)
        .click();
    }
    cy.get('[data-cy="Topics"]')
      .find('i')
      .click();
    cy.get('[data-cy="StudentQuestionTitle"]').type(title);
    cy.get('[data-cy="StudentQuestionContent"]').type(content);
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
  cy.contains(`${message}`)
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
    cy.contains('Cancel').click();
  }
);

Cypress.Commands.add(
  'createNoContentStudentQuestion',
  (title, topic, options, correctOption) => {
    cy.createStudentQuestion(title, '', topic, options, correctOption);
    cy.errorMessageClose(
      'Question must have title, content, and at least one topic'
    );
    cy.contains('Cancel').click();
  }
);

Cypress.Commands.add(
  'createNoTopicsStudentQuestion',
  (title, content, options, correctOption) => {
    cy.createStudentQuestion(title, content, [], options, correctOption);
    cy.errorMessageClose(
      'Question must have title, content, and at least one topic'
    );
    cy.contains('Cancel').click();
  }
);

Cypress.Commands.add(
  'createNoOptionsStudentQuestion',
  (title, content, topic, correctOption) => {
    cy.createStudentQuestion(title, content, topic, [], correctOption);
    cy.errorMessageClose(
      
    )
  }
)
