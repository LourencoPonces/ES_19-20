describe('getAvailableTournaments', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
  });

  afterEach(() => {
    cy.contains('Logout').click();
  });

  it('login creates a tournament with default available date and checks if is available', () => {
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    // Wait for fetching topics data
    cy.wait(1000);

    let title = 'test' + Date.now().toString();
    let numberOfQuestions = 10;
    let creator = Store.getters.getUser;

    cy.createTournament(title, numberOfQuestions);
    cy.assertAvailableTournaments(creator, 1);
  });

  it('login creates a tournament with default available date', () => {
    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-available]').click();
    // Wait for fetching topics data
    cy.wait(1000);

    let title = "severalTournamentsTitle"
    let numberOfQuestions = 10;
    let creator = Store.getters.getUser;

    cy.createTournament(title, numberOfQuestions);
    cy.createTournament(title, numberOfQuestions);
    cy.createTournament(title, numberOfQuestions);
    cy.createTournament(title, numberOfQuestions);
    cy.createTournament(title, numberOfQuestions);
    cy.assertAvailableTournaments(creator, 5);
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
