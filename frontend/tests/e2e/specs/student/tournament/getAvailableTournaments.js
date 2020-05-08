describe('getAvailableTournaments', () => {
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

  it('creates a tournament with created status, checks if it is not available, delete at the end', () => {
    let title = 'test' + Date.now().toString();

    cy.createTournament(title, true, true, true, true);
    cy.wait(500);
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    cy.wait(1000);
    cy.assertTournaments(title, 0, 9, 1);
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-created]').click();
    cy.wait(1000);
    cy.deleteTournament(title);
  });

  it('creates a tournament with available status, checks if it is available, delete at the end', () => {
    let title = 'test' + Date.now().toString();

    cy.createTournament(title, false, true, true, true);
    cy.wait(500);
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    cy.wait(1000);
    cy.assertTournaments(title, 1, 9, 1);
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-created]').click();
    cy.wait(1000);
    cy.deleteTournament(title);
  });

  it('creates a tournament with running status, checks if it is not available, delete at the end', () => {
    let title = 'test' + Date.now().toString();

    cy.createTournament(title, false, false, true, true);
    cy.wait(500);
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    cy.wait(1000);
    cy.assertTournaments(title, 0, 9, 1);
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-created]').click();
    cy.wait(1000);
    cy.deleteTournament(title);
  });

  it('creates a tournament with finished status, checks if it is not available, delete at the end', () => {
    let title = 'test' + Date.now().toString();

    cy.createTournament(title, false, false, false, true);
    cy.wait(500);
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    cy.wait(1000);
    cy.assertTournaments(title, 0, 9, 1);
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-created]').click();
    cy.wait(1000);
    cy.deleteTournament(title);
  });

  it('creates 3 tournaments with available status, checks if they are available, deletes all at the end', () => {
    let title1 = 'test1' + Date.now().toString();
    let title2 = 'test2' + Date.now().toString();
    let title3 = 'test3' + Date.now().toString();

    cy.createTournament(title1, false, true, true, true);
    cy.wait(1000);
    cy.createTournament(title2, false, true, true, true);
    cy.wait(1000);
    cy.createTournament(title3, false, true, true, true);
    cy.wait(1000);
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    cy.wait(1000);
    cy.assertTournaments(title1, 3, 9, 1);
    cy.assertTournaments(title2, 3, 9, 1);
    cy.assertTournaments(title3, 3, 9, 1);
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-created]').click();
    cy.wait(1000);
    cy.deleteTournament(title1);
    cy.wait(500);
    cy.deleteTournament(title2);
    cy.wait(500);
    cy.deleteTournament(title3);
  });
});
