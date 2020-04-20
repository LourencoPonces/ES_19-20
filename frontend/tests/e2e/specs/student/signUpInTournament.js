describe('Sign-up in Tournament', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
  });

  afterEach(() => {
    cy.logout();
  });

  it('login creates a tournament with default available date, checks if it is sign-up and deletes at the end', () => {
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    // Wait for fetching topics data
    cy.wait(1000);
    let title = 'test' + Date.now().toString();

    cy.createTournament(title);
    cy.wait(1000);
    cy.assertSignUpTournament(title);
    cy.deleteTournament(title);
    cy.wait(1000);
  });

});
