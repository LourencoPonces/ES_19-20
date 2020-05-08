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

Cypress.Commands.add('demoTeacherLogin', () => {
  cy.visit('/');
  cy.get('[data-cy="teacherButton"]').click();
});

Cypress.Commands.add('demoStudentLogin', () => {
  cy.visit('/');
  cy.get('[data-cy="studentButton"]').click();
});

Cypress.Commands.add('logout', () => {
  // Work around VMenu bug
  // this handler runs at most once, and only matches a specific error
  cy.once('uncaught:exception', (error, _) => {
    // eslint-disable-next-line prettier/prettier
    if (error.message == "Cannot read property 'contains' of undefined") {
      return true;
    }

    return false;
  });

  cy.get('[data-cy="logoutButton"]').click();
});

Cypress.Commands.add('createCourseExecution', (name, acronym, academicTerm) => {
  cy.get('[data-cy="createButton"]').click();
  cy.get('[data-cy="courseExecutionNameInput"]').type(name);
  cy.get('[data-cy="courseExecutionAcronymInput"]').type(acronym);
  cy.get('[data-cy="courseExecutionAcademicTermInput"]').type(academicTerm);
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
    cy.get('[data-cy="courseExecutionAcronymInput"]').type(acronym);
    cy.get('[data-cy="courseExecutionAcademicTermInput"]').type(academicTerm);
    cy.get('[data-cy="saveButton"]').click();
  }
);

/* CLARIFICATION REQUESTS TESTS */

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
      cy.get('[data-cy="nextQuestion"]').click();
    }
  }
});

Cypress.Commands.add('goToMyClarifications', () => {
  cy.get('[data-cy="my-area"]').click();
  cy.get('[data-cy="clarifications"]').click();
});

Cypress.Commands.add('deleteAllRequests', n => {
  if (n > 0) {
    for (i = 0; i < n; i++) {
      cy.wait(500);
      cy.get('[data-cy="table"]')
        .find('tbody')
        .children()
        .should('have.length', n - i)
        .first();

      cy.get('[data-cy="delete"]')
        .first()
        .click();
    }
  }
});

Cypress.Commands.add('editClarificationRequest', content => {
  cy.wait(500);
  cy.get('[data-cy="edit"]')
    .first()
    .click();

  cy.get('.v-dialog')
    .get('[data-cy="inputNewContent"]')
    .type(content);

  cy.get('.v-dialog')
    .get('[data-cy="actions"]')
    .children()
    .last()
    .click();

  cy.get('[data-cy="table"]')
    .find('tbody')
    .children()
    .should('have.length', 1)
    .first()
    .should('contain.text', content);
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

Cypress.Commands.add('deleteClarificationMessage', requestText => {
  cy.get('[data-cy="management"]').click();
  cy.get('[data-cy="teacherClarifications"]').click();
  cy.get(
    `[data-cy^="answerClarification-${requestText.slice(0, 15)}"]`
  ).click();

  cy.get('[data-cy="answerDelete"]').click();
});

Cypress.Commands.add('changeClarificationRequestStatus', requestText => {
  cy.get('[data-cy="management"]').click();
  cy.get('[data-cy="teacherClarifications"]').click();
  cy.get(`[data-cy^="private-${requestText.slice(0, 15)}"]`)
    .first()
    .should('exist');
  cy.get(`[data-cy^="changeStatus-${requestText.slice(0, 15)}"]`)
    .first()
    .click();
  cy.get(`[data-cy^="public-${requestText.slice(0, 15)}"]`)
    .first()
    .should('exist');
});

Cypress.Commands.add('checkRequestInDiscussion', requestText => {
  cy.get('[data-cy="quizzes"]').click();
  cy.get('[data-cy="solved"]').click();
  cy.get('[data-cy="solvedList"]')
    .children()
    .first()
    .click();
  cy.get('[data-cy="questionRequests"]')
    .children()
    .should('have.length', 1)
    .first()
    .should('contain.text', requestText);
});

/* STUDENT QUESTION TESTS */

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
      if (correctOption > 0 && correctOption < 5) {
        cy.get(`[data-cy="CorrectOption${correctOption}"]`)
          .parent()
          .click();
      }
    });

    for (let i = 1; i < options.length + 1; i++) {
      cy.get(`[data-cy  =Option${i}]`).type(options[i - 1]);
    }

    cy.get('[data-cy="SaveStudentQuestion"]').click();
  }
);

Cypress.Commands.add(
  'evaluateStudentQuestion',
  (title, prevStatus, status, justification) => {
    // select evaluate question
    cy.contains(title)
      .parents('tr')
      .eq(0)
      .contains(prevStatus)
      .click();

    // select drop down
    cy.get('[data-cy="status-dropdown"]')
      .contains(prevStatus)
      .click();

    // select evaluation status
    cy.get('.v-list-item__content')
      .contains(status)
      .click();

    // write justification
    if (justification != null && justification != '') {
      cy.get('[data-cy="justification-text"]')
        .clear()
        .type(justification);
    }

    // select evaluate button
    cy.get('[data-cy="do-evaluate"]').click();
  }
);

Cypress.Commands.add(
  'assertStudentQuestionEvaluation',
  (questionTitle, status, justification) => {
    // assert status
    cy.contains(questionTitle)
      .parents('tr')
      .eq(0)
      .find('[data-cy="evaluate"]')
      .should('have.text', status);

    if (justification.length !== 0) {
      // assert justification
      cy.contains(questionTitle)
        .parents('tr')
        .eq(0)
        .find('[data-cy="showJustification"]')
        .click();

      cy.get('[data-cy="justification-text"]').should($el => {
        let elem = $el[0];

        // if promoted question, justification is in span
        if (elem.tagName === 'SPAN') {
          expect(elem.innerText).to.equal(justification);
        } else {
          // else (evaluating), justification is in textarea
          expect(elem.value).to.equal(justification);
        }
      });
      cy.get('[data-cy=CancelEvaluation]').click();
    }
  }
);

Cypress.Commands.add(
    'assertQuestionExists',
    (questionTitle) => {
        // go to questions
        cy.get('[data-cy="management"]').click();
        cy.get('[data-cy="questions"]').click();

        // look for question title
        cy.get('[data-cy="search-input"]').clear().type(questionTitle);

        // assert question exists
        cy.contains(questionTitle);
    }
)

Cypress.Commands.add(
  'studentAssertEvaluation',
  (questionTitle, status, justification) => {
    // assert status
    cy.contains(questionTitle)
      .parents('tr')
      .eq(0)
      .find('[data-cy="showStatus"]')
      .should('have.text', status);

    if (justification == null) {
      // assert no justification
      cy.contains(questionTitle)
        .parent()
        .children()
        .should('not.have.text', 'Justification');
    } else {
      // assert justification
      cy.contains(questionTitle)
        .parents('tr')
        .eq(0)
        .find('[data-cy="showStatus"]')
        .scrollIntoView()
        .click();

      cy.get('[data-cy="justification-text"]').should(
        'have.text',
        justification
      );
    }
  }
);

Cypress.Commands.add('errorMessageClose', message => {
  cy.contains(message)
    .parent()
    .find('button')
    .click();
});

Cypress.Commands.add(
  'editAndPromoteStudentQuestion',
  (questionTitle, newTitle, newContent, oldTopics, newTopics, newOptions) => {
    cy.contains(questionTitle).rightclick();

    cy.fillStudentQuestionDialog(
      newTitle,
      newContent,
      oldTopics,
      newTopics,
      newOptions
    );
  }
);

Cypress.Commands.add(
  'editStudentQuestion',
  (questionTitle, newTitle, newContent, oldTopics, newTopics, newOptions) => {
    cy.contains(questionTitle)
      .parents('tr')
      .eq(0)
      .find('[data-cy="editStudentQuestion"]')
      .click();

    cy.fillStudentQuestionDialog(
      newTitle,
      newContent,
      oldTopics,
      newTopics,
      newOptions
    );
  }
);

Cypress.Commands.add(
  'duplicateStudentQuestion',
  (questionTitle, newTitle, newContent, oldTopics, newTopics, newOptions) => {
    cy.contains(questionTitle)
      .parents('tr')
      .eq(0)
      .find('[data-cy="duplicateStudentQuestion"]')
      .click();

    cy.fillStudentQuestionDialog(
      newTitle,
      newContent,
      oldTopics,
      newTopics,
      newOptions
    );
  }
);

Cypress.Commands.add(
  'fillStudentQuestionDialog',
  (newTitle, newContent, oldTopics, newTopics, newOptions) => {
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
  (title, afterAvailable, afterRunning, afterConclusion, hasAvailable) => {
    let availableNr = 1;
    let runningNr = 2;
    let conclusionNr = 3;
    let nQuestions = '10';

    cy.get('[data-cy="newTournament"]').click({ force: true });
    // wait for dialog to open
    cy.wait(500);
    cy.get('[data-cy="title"]').type(title);
    cy.get('[data-cy="numberOfQuestions"').type(nQuestions);

    // --------- Available Date ---------
    if (hasAvailable) {
      cy.contains('.v-label', 'Available Date').click({ force: true });
      cy.wait(500);
      if (afterAvailable)
        cy.get('.mdi-chevron-right').click({ multiple: true, force: true });
      else cy.get('.mdi-chevron-left').click({ multiple: true, force: true });
      cy.wait(500);
      cy.get(
        `.v-date-picker-table > table > tbody > :nth-child(3) > :nth-child(${availableNr}) > .v-btn`
      )
        .first()
        .click({ force: true });
      // click ok, contains('OK') doesn't work...
      cy.get('.v-card__actions > .green--text > .v-btn__content').click({
        multiple: true,
        force: true
      });
    }
    // --------- Available Date ---------

    // --------- Running Date ---------
    cy.contains('.v-label', 'Running Date').click({ force: true });
    cy.wait(500);
    if (afterRunning)
      cy.get('.mdi-chevron-right').click({ multiple: true, force: true });
    else cy.get('.mdi-chevron-left').click({ multiple: true, force: true });
    cy.wait(500);
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
    // --------- Running Date ---------

    // --------- Conclusion Date ---------
    cy.contains('.v-label', 'Conclusion Date').click({ force: true });
    cy.wait(500);
    if (afterConclusion)
      cy.get('.mdi-chevron-right').click({ multiple: true, force: true });
    else cy.get('.mdi-chevron-left').click({ multiple: true, force: true });
    cy.wait(500);
    cy.get(
      `.v-date-picker-table > table > tbody > :nth-child(3) > :nth-child(${conclusionNr}) > .v-btn`
    )
      .last()
      .click({ force: true });
    // click ok, contains('OK') doesn't work...
    cy.get('.v-card__actions > .green--text > .v-btn__content').click({
      multiple: true,
      force: true
    });
    // --------- Conclusion Date ---------

    cy.get('[data-cy="topics"').click();
    cy.get('[role=listbox]')
      .children()
      .first()
      .click({ force: true });

    cy.get('[data-cy="saveTournament"]').click();
  }
);

Cypress.Commands.add(
  'createTournament',
  (title, afterAvailable, afterRunning, afterConclusion, hasAvailable) => {
    let availableNr = 1;
    let runningNr = 2;
    let conclusionNr = 3;
    let nQuestions = '1';

    cy.get('[data-cy="newTournament"]').click({ force: true });
    // wait for dialog to open
    cy.wait(500);
    cy.get('[data-cy="title"]').type(title);
    cy.get('[data-cy="numberOfQuestions"').type(nQuestions);

    // --------- Available Date ---------
    if (hasAvailable) {
      cy.contains('.v-label', 'Available Date').click({ force: true });
      cy.wait(500);
      if (afterAvailable)
        cy.get('.mdi-chevron-right').click({ multiple: true, force: true });
      else cy.get('.mdi-chevron-left').click({ multiple: true, force: true });
      cy.wait(500);
      cy.get(
        `.v-date-picker-table > table > tbody > :nth-child(3) > :nth-child(${availableNr}) > .v-btn`
      )
        .first()
        .click({ force: true });
      // click ok, contains('OK') doesn't work...
      cy.get('.v-card__actions > .green--text > .v-btn__content').click({
        multiple: true,
        force: true
      });
    }
    // --------- Available Date ---------

    // --------- Running Date ---------
    cy.contains('.v-label', 'Running Date').click({ force: true });
    cy.wait(500);
    if (afterRunning)
      cy.get('.mdi-chevron-right').click({ multiple: true, force: true });
    else cy.get('.mdi-chevron-left').click({ multiple: true, force: true });
    cy.wait(500);
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
    // --------- Running Date ---------

    // --------- Conclusion Date ---------
    cy.contains('.v-label', 'Conclusion Date').click({ force: true });
    cy.wait(500);
    if (afterConclusion)
      cy.get('.mdi-chevron-right').click({ multiple: true, force: true });
    else cy.get('.mdi-chevron-left').click({ multiple: true, force: true });
    cy.wait(500);
    cy.get(
      `.v-date-picker-table > table > tbody > :nth-child(3) > :nth-child(${conclusionNr}) > .v-btn`
    )
      .last()
      .click({ force: true });
    // click ok, contains('OK') doesn't work...
    cy.get('.v-card__actions > .green--text > .v-btn__content').click({
      multiple: true,
      force: true
    });
    // --------- Conclusion Date ---------

    cy.get('[data-cy="topics"').click();
    cy.get('[role=listbox]')
      .children()
      .first()
      .next()
      .click({ force: true });

    cy.get('[data-cy="saveTournament"]').click();
  }
);

Cypress.Commands.add('deleteTournament', title => {
  cy.contains(title)
    .parent()
    .find('[data-cy="deleteTournament"]')
    .click();
});

Cypress.Commands.add('assertTournaments', (title, number, col, topics) => {
    if (number == 0){
        cy.get('tr')
            .get('td')
            .should('contain', 'No Available Tournaments')
    }
    else {
        cy.contains(title)
            .parent()
            .children()
            .should('have.length', col);

        cy.contains(title)
            .parent()
            .find('[data-cy="topics-list"]')
            .should('have.length', topics);

        cy.get('table')
            .get('tbody')
            .children()
            .should('have.length', number);
    }
});

Cypress.Commands.add('assertSignUpTournament', title => {
    cy.get('table')
        .get('tbody')
        .get('tr')
        .get('td')
        .eq(8)
        .get('div[aria-label="true"]')
});

// DASHBOARD COMMANDS
Cypress.Commands.add('makePrivate', dataCy => {
  cy.get(dataCy)
    .find('button')
    .then($btn => {
      if ($btn.hasClass('fas fa-eye')) {
        $btn.click();
      }
    });
});

Cypress.Commands.add('makePublic', dataCy => {
  cy.get(dataCy)
    .find('button')
    .then($btn => {
      if ($btn.hasClass('fas fa-eye-slash')) {
        $btn.click();
      }
    });
});

Cypress.Commands.add('openDashboardStatsDialog', student => {
  cy.contains(student)
    .parents('tr')
    .eq(0)
    .find('[data-cy="showDashboardStatsButton"]')
    .click();
});

Cypress.Commands.add('checkVisibility', (statRow, content) => {
  cy.get(statRow).contains(content);
});
