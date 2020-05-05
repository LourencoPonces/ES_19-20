const visibility = {
  PUBLIC: 'PUBLIC',
  PRIVATE: 'PRIVATE'
};

export default class DashboardStats {
  requestsSubmittedVisibility: string = visibility.PRIVATE;
  requestsSubmittedStat: number = 0;
  publicRequestsVisibility: string = visibility.PRIVATE;
  publicRequestsStat: number = 0;

  constructor(jsonObj?: DashboardStats) {
    if (jsonObj) {
      this.requestsSubmittedVisibility = jsonObj.requestsSubmittedVisibility;
      this.requestsSubmittedStat = jsonObj.requestsSubmittedStat;
      this.publicRequestsVisibility = jsonObj.publicRequestsVisibility;
      this.publicRequestsStat = jsonObj.publicRequestsStat;
    }
  }

  isPublic(stat: string): boolean {
    return stat === visibility.PUBLIC;
  }
}
