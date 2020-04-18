export interface User {
  id: number;
  username: string;
  authorities: string[];
}

export interface UserFull extends User {
  password: string;
  confirmPassword: string;
  role: string;
}
