const visibility = {
  PUBLIC: 'PUBLIC',
  PRIVATE: 'PRIVATE'
};

enum StatsNames {
  SUBMITTED_QUESTIONS = 'submittedQuestions',
  APPROVED_QUESTIONS = 'approvedQuestions',
  REQUESTS_SUBMITTED = 'requestsSubmitted',
  PUBLIC_REQUESTS = 'publicRequests'
}

export default class DashboardStats {
  statsValues;
  statsVisibility;
  statsNames = StatsNames;

  constructor(jsonObj?: DashboardStats) {
    if (jsonObj) {
      this.statsValues = jsonObj.statsValues;
      this.statsVisibility = jsonObj.statsVisibility;
    }
  }

  isPublic(stat: StatsNames): boolean {
    return this.statsVisibility[stat] === visibility.PUBLIC;
  }

  getStatValue(stat: StatsNames): number {
    return this.statsValues[stat];
  }
}
