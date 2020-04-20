describe('getAvailableTournaments', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
  });

  afterEach(() => {
    cy.contains('Logout').click();
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
    let numberOfQuestions = 10;

    cy.createTournament(title1);
    cy.wait(1000);
    cy.createTournament(title2);
    cy.wait(1000);
    cy.createTournament(title3);
    cy.wait(1000);
    cy.assertAvailableTournaments(title2);
    cy.wait(1000);
    cy.deleteTournament(title1);
    cy.wait(1000);
    cy.deleteTournament(title2);
    cy.wait(1000);
    cy.deleteTournament(title3);
  });

  /*it('login creates a tournament with a chosen available date', () => {
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
  });*/
});
