describe('getSignedUpRunningTournaments', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
  });

  afterEach(() => {
    cy.logout();
  });

  it('creates a tournament in running state and checks its presence in Running page', () => {
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-created]').click();
    // Wait for fetching topics data
    cy.wait(1000);

    let title = 'test' + Date.now().toString();

    cy.createTournament(title, false, false, true, true);
    cy.wait(1000);

    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-running]').click();
    cy.contains(title);

    cy.wait(1000);
    cy.deleteTournament(title);
  });
});
