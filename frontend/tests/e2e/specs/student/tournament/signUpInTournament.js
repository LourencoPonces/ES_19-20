describe('Sign-up in Tournament', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
  });

  afterEach(() => {
    cy.logout();
  });

  it('login creates a tournament, checks sign-up status and deletes', () => {
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    // Wait for fetching topics data
    cy.wait(1000);
    let title = 'test' + Date.now().toString();

    cy.createTournament(title, false, true, true);
    cy.wait(1000);
    cy.assertSignUpTournament(title, 10, 1);
    cy.deleteTournament(title);
    cy.wait(1000);
  });

});
