export class User {
  id: number;
  username: string;
  authorities?: Role[];
}
export class Role {
  authority: string;
}
export class UserFull extends User {
  password: string;
  confirmPassword: string;
  role: string;
}