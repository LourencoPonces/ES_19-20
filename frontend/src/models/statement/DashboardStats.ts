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
  id: number;
  statsValues;
  statsVisibility;
  statsNames = StatsNames;

  constructor(jsonObj?: DashboardStats) {
    if (jsonObj) {
      this.id = jsonObj.id;
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

  makePublic(stat: StatsNames) {
    this.statsVisibility[stat] = visibility.PUBLIC;
  }

  makePrivate(stat: StatsNames) {
    this.statsVisibility[stat] = visibility.PRIVATE;
  }
}
