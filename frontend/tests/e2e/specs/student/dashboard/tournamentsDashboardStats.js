describe('Tournaments', () => {
  const content = 'Test tournaments content.';
  const demoStudent = 'Demo Student';
  const PRIVATE = 'Private';
  
  beforeEach(() => {
    cy.demoStudentLogin();
  });

  afterEach(() => {
    // clean exit
    cy.contains('Demo Course').click();
    cy.logout();
  });

  it('change demo student dashboard stats visibility', () => {
    // make the 2 stats private and check
    cy.get('[data-cy="dashboardButton"]').click();
    const tournamentsParticipatedDiv = '[data-cy="tournamentsParticipatedDiv"]';
    const tournamentsScoreDiv = '[data-cy="tournamentsScoreDiv"]';
    const tournamentsParticipatedRow = '[data-cy="tournamentsParticipatedRow"]';
    const tournamentsScoreRow = '[data-cy="tournamentsScoreRow"]';

    cy.makePrivate(tournamentsParticipatedDiv);
    cy.makePrivate(tournamentsScoreDiv);

    cy.openDashboardStatsDialog(demoStudent);
    cy.checkVisibility(tournamentsParticipatedRow, PRIVATE);
    cy.checkVisibility(tournamentsScoreRow, PRIVATE);
    cy.get('[data-cy="closeDashboardTable"]').click();

    // make the 2 stats public and check
    cy.makePublic(tournamentsParticipatedDiv);
    cy.makePublic(tournamentsScoreDiv);

    cy.openDashboardStatsDialog(demoStudent);
    cy.checkVisibility(tournamentsParticipatedRow, '0');
    cy.checkVisibility(tournamentsScoreRow, '0');
    cy.get('[data-cy="closeDashboardTable"]').click();
  });

  it('check no tournaments participated and score', () => {
    cy.get('[data-cy="dashboardButton"]').click();
    cy.get('[data-cy="tournamentsParticipated"]').contains('0');
    cy.get('[data-cy="tournamentsScore"]').contains('0');
  });

  it('create 2 tournaments, ', () => {
    let title1 = 'test' + Date.now().toString();
    let title2 = 'test' + Date.now().toString();

    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-created]').click();
    // Wait for fetching topics data
    cy.wait(500);

    cy.createTournament(title1, true, true, true, true);
    cy.wait(1000);
    cy.createTournament(title2, true, true, true, true);
    cy.wait(1000);

    cy.get('[data-cy="dashboardButton"]').click();
    cy.get('[data-cy="tournamentsParticipated"]').contains('2');
    cy.get('[data-cy="tournamentsScore"]').contains('0');

    cy.get('[data-cy=tournaments]').click();
    cy.get('[data-cy=tournaments-created]').click();
    // Wait for fetching topics data
    cy.wait(500);
    cy.deleteTournament(title1);
    cy.wait(500);
    cy.deleteTournament(title2);
    cy.wait(500);
  });
});
