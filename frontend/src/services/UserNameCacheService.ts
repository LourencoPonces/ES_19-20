import User from '@/models/user/User';

export default class UserNameCacheService {
  private static names: Record<string, string> = Object.create(null);

  static addUser(user: User): void {
    this.names[user.username] = user.name;
  }

  static bulkAdd(names: Record<string, string>): void {
    for (const username in names) {
      this.names[username] = names[username];
    }
  }

  static getName(username: string): string {
    return this.names[username];
  }
}
