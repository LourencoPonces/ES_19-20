describe('Sign-up in Tournament', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-created]').click();
    // Wait for fetching topics data
    cy.wait(1000);
  });

  afterEach(() => {
    cy.logout();
  });

  it('creates a tournament, checks sign-up status, deletes it at the end', () => {
    let title = 'test' + Date.now().toString();

    cy.createTournament(title, false, true, true, true);
    cy.wait(1000);
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    cy.wait(1000);
    cy.assertSignUpTournament(title, 10, 1);
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-created]').click();
    cy.wait(1000);
    cy.deleteTournament(title);
    cy.wait(1000);
  });

});
