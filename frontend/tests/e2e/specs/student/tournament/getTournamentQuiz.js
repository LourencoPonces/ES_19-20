describe('Get tournament quiz', () => {
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

  it('Tries to solve an un-generated quiz', () => {
    let title = 'test' + Date.now().toString();

    cy.createTournament(title, false, false, true, true);
    cy.wait(1000);
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-running]').click();
    cy.wait(1000);
    cy.contains(title)
      .parent()
      .find('[data-cy="solveTournament"]')
      .click();
    // cy.get('[data-cy=tournaments]').click();
    // cy.get('[data-cy=tournaments-created]').click();
    // cy.wait(1000);
    cy.errorMessageClose(
      "Error: There isn't any quiz generated for this tournament"
    );
    cy.deleteTournament(title);
    cy.wait(1000);
  });
});
