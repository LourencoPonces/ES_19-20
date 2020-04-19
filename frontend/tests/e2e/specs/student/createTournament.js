describe('Create Tournament', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
  });

  afterEach(() => {
    cy.contains('Logout').click();
  });

  it('login creates a tournament with default available date', () => {
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    // Wait for fetching topics data
    cy.wait(1000);
    cy.createTournament('test' + Date.now().toString(), 10);
  });

  it('login creates a tournament with a chosen available date', () => {
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    // Wait for fetching topics data
    cy.wait(1000);
    cy.createTournament('test' + Date.now().toString(), 10, true);
  });

  it('login creates a tournament with invalid dates', () => {
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    // Wait for fetching topics data
    cy.wait(1000);
    cy.createTournament('test' + Date.now().toString(), 10, true, [
      'conclusion',
      'running',
      'available'
    ]);
    cy.errorMessageClose(
      'Error: Field Available date of tournament is not consistent'
    );
  });
});
