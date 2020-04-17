describe('Create Tournament', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
  });

  afterEach(() => {
    // cy.contains('Logout').click();
  });

  it('login creates a tournament', () => {
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    // Wait for fetching topics data
    cy.wait(1000);
    cy.createTournament();
  });
});
