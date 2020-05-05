describe('Create Tournament', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
  });

  afterEach(() => {
    cy.logout();
  });

  it('login creates a tournament with default available date', () => {
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    // Wait for fetching topics data
    cy.wait(1000);
    let title = 'test' + Date.now().toString();
    cy.createTournament(title, false, true, true, false);
    cy.wait(1000);
    cy.deleteTournament(title);
    cy.wait(1000);
  });

  it('login creates a tournament with a chosen available date', () => {
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    // Wait for fetching topics data
    cy.wait(1000);
    let title = 'test' + Date.now().toString();
    cy.createTournament(title, false, true, true, true);
    cy.wait(1000);
    cy.deleteTournament(title);
    cy.wait(1000);
  });

  it('login creates a tournament with invalid dates', () => {
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    // Wait for fetching topics data
    cy.wait(1000);
    cy.createTournament('test' + Date.now().toString(), false, true, false, true);
    cy.errorMessageClose(
      'Error: Field Running date of tournament is not consistent'
    );
  });
});
