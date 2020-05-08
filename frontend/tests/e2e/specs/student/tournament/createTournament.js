describe('Create Tournament', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-created]').click();
    // Wait for fetching topics data
    cy.wait(500);
  });

  afterEach(() => {
    cy.logout();
  });

  it('creates a tournament with default available date, delete at the end', () => {
    let title = 'test' + Date.now().toString();
    cy.createTournament(title, false, true, true, false);
    cy.wait(500);
    cy.deleteTournament(title);
    cy.wait(500);
  });

  it('creates a tournament with a chosen available date, delete at the end', () => {
    let title = 'test' + Date.now().toString();
    cy.createTournament(title, false, true, true, true);
    cy.wait(500);
    cy.deleteTournament(title);
    cy.wait(500);
  });

  it('creates a tournament with created status, delete at the end', () => {
    let title = 'test' + Date.now().toString();
    cy.createTournament(title, true, true, true, true);
    cy.wait(500);
    cy.deleteTournament(title);
    cy.wait(500);
  });

  it('creates a tournament with running status, delete at the end', () => {
    let title = 'test' + Date.now().toString();
    cy.createTournament(title, false, false, true, true);
    cy.wait(500);
    cy.deleteTournament(title);
    cy.wait(500);
  });

  it('creates a tournament with running status, delete it at the end', () => {
    let title = 'test' + Date.now().toString();
    cy.createTournament(title, false, false, false, true);
    cy.wait(500);
    cy.deleteTournament(title);
    cy.wait(500);
  });

  it('creates a tournament with invalid dates', () => {
    cy.createTournament('test' + Date.now().toString(), false, true, false, true);
    cy.errorMessageClose(
      'Error: Field Running date of tournament is not consistent'
    );
  });
});
