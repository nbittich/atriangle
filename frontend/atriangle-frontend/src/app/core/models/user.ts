export class User {
  id: number;
  username: string;
  authorities: string[];
}
export class UserFull extends User {
  password: string;
  confirmPassword: string;
  role: string;
}
