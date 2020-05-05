export class Student {
  id!: number;
  number!: number;
  username: string | null = null;
  name: string | null = null;
  numberOfTeacherQuizzes!: number;
  numberOfStudentQuizzes!: number;
  numberOfAnswers!: number;
  numberOfTeacherAnswers!: number;
  percentageOfCorrectAnswers!: number;
  percentageOfCorrectTeacherAnswers!: number;

  constructor(jsonObj?: Student) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.number = jsonObj.number;
      this.username = jsonObj.username;
      this.name = jsonObj.name;
      this.numberOfTeacherQuizzes = jsonObj.numberOfTeacherQuizzes;
      this.numberOfStudentQuizzes = jsonObj.numberOfStudentQuizzes;
      this.numberOfAnswers = jsonObj.numberOfAnswers;
      this.numberOfTeacherAnswers = jsonObj.numberOfTeacherAnswers;
      this.percentageOfCorrectAnswers = jsonObj.percentageOfCorrectAnswers;
      this.percentageOfCorrectTeacherAnswers =
        jsonObj.percentageOfCorrectTeacherAnswers;
    }
  }
}
