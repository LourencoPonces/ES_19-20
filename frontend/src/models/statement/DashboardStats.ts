const visibility = {
  PUBLIC: 'PUBLIC',
  PRIVATE: 'PRIVATE'
};

export default class DashboardStats {
  testStatVisibility: string = visibility.PUBLIC;
  testStatValue: number = 0;

  constructor(jsonObj?: DashboardStats) {
    if (jsonObj) {
      this.testStatVisibility = jsonObj.testStatVisibility;
      this.testStatValue = jsonObj.testStatValue;
    }
  }

  isPublic(stat: string): boolean {
    return stat === visibility.PUBLIC;
  }

  getVisibilityColor(stat: string): string {
    switch (stat) {
      case visibility.PRIVATE:
        return 'red';
      case visibility.PUBLIC:
        return 'green';
      default:
        throw new Error('Invalid status');
    }
  }
}
