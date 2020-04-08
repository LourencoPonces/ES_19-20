import User from '@/models/user/User';
import Topic from '@/models/management/Topic';

export class Tournament {
  id: number | null = null;
  number!: number; // TODO: Does this correspond to the key?
  title!: string;
  numberOfQuestions!: number;

  // TODO: Why are the dates undefined (check Quiz.ts)
  creationDate!: string | undefined;
  availableDate!: string | undefined;
  runningDate!: string | undefined;
  conclusionDate!: string | undefined;
  isCancelled: boolean = false;
  creator!: User;
  participants: User[] = [];
  topics!: Topic[];

  constructor(jsonObj: Tournament) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.number = jsonObj.number;
      this.title = jsonObj.title;
      this.numberOfQuestions = jsonObj.numberOfQuestions;
      this.creationDate = jsonObj.creationDate;
      this.availableDate = jsonObj.availableDate;
      this.runningDate = jsonObj.runningDate;
      this.conclusionDate = jsonObj.conclusionDate;
      this.isCancelled = jsonObj.isCancelled;
      this.creator = jsonObj.creator;

      if (jsonObj.participants) {
        this.participants = jsonObj.participants.map(
          (user: User) => new User(user)
        );
      }

      if (jsonObj.topics) {
        this.topics = jsonObj.topics.map((topic: Topic) => new Topic(topic));
      }
    }
  }
}
