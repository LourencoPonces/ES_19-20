describe('getCreatedTournaments', () => {
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

  it('creates a tournament and checks if it is listed, deletes it at the end', () => {
    let title = "Tournament - " + Date.now().toString();
    cy.createTournament(title, true, true, true, true);
    cy.assertAvailableTournaments(title, 9, 1);
    cy.deleteTournament(title);
  });

  it('creates an available tournament and checks if it is listed, deletes it at the end', () => {
    let title = "Tournament - " + Date.now().toString();
    cy.createTournament(title, false, true, true, true);
    cy.assertAvailableTournaments(title, 9, 1);
    cy.deleteTournament(title);
  });

  it('creates a running tournament and checks if it is listed, deletes it at the end', () => {
    let title = "Tournament - " + Date.now().toString();
    cy.createTournament(title, false, false, true, true);
    cy.assertAvailableTournaments(title, 9, 1);
    cy.deleteTournament(title);
  });

  it('creates a ended tournament and checks if it is listed, deletes it at the end', () => {
    let title = "Tournament - " + Date.now().toString();
    cy.createTournament(title, false, false, false, true);
    cy.assertAvailableTournaments(title, 9, 1);
    cy.deleteTournament(title);
  });
});
