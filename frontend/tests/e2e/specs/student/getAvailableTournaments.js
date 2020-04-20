describe('getAvailableTournaments', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
  });

  afterEach(() => {
    cy.logout();
  });

  it('login creates a tournament with default available date and checks if it is available and deletes at the end', () => {
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    // Wait for fetching topics data
    cy.wait(1000);

    let title = 'test' + Date.now().toString();

    cy.createTournament(title);
    cy.wait(1000);
    cy.assertAvailableTournaments(title);
    cy.wait(1000);
    cy.deleteTournament(title);
  });

  it('login creates 3 tournaments with default available date and checks if they are available and deletes at the end', () => {
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    // Wait for fetching topics data
    cy.wait(1000);

    let title1 = 'test1'
    let title2 = 'test2'
    let title3 = 'test3'

    cy.createTournament(title1);
    cy.wait(1000);
    cy.createTournament(title2);
    cy.wait(1000);
    cy.createTournament(title3);
    cy.wait(1000);
    cy.assertAvailableTournaments(title1);
    cy.assertAvailableTournaments(title2);
    cy.assertAvailableTournaments(title3);
    cy.wait(1000);
    cy.deleteTournament(title1);
    cy.wait(1000);
    cy.deleteTournament(title2);
    cy.wait(1000);
    cy.deleteTournament(title3);
  });
});
