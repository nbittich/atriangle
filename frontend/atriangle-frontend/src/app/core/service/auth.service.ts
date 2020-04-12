import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { map } from 'rxjs/operators';

import { User } from '../models/user';
import {environment} from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  setTimer: Subject<boolean> = new Subject<boolean>();

  constructor(private http: HttpClient, private router: Router) {}

  logout(redirect: boolean = true): void {
    localStorage.removeItem('xAuthToken');
    localStorage.removeItem('user');
    if (redirect) {
      this.router.navigate(['']);
    }
  }

  login(username: string, password: string): Observable<void> {
    const headers = new HttpHeaders({
      Authorization: `Basic ${window.btoa(`${username}:${unescape(encodeURIComponent(password))}`)}`
    });
    return this.http
      .post<User>(
        `${environment.backendUrl}/api/user/info`,
        {},
        {
          headers,
          observe: 'response'
        }
      )
      .pipe(
        map((resp: HttpResponse<User>) => {
          const token = resp.headers.get('X-Auth-Token');
          if (token && token.length) {
            localStorage.setItem('xAuthToken', token);
            this.setTimer.next();
            this.setUser(resp.body);
          }
        })
      );
  }

  setTokenTimer(showModal?: boolean): void {
    this.setTimer.next(showModal);
  }

  isLoggedIn(): boolean {
    return this.getUser() !== null;
  }

  getUser(): User {
    const user = localStorage.getItem('user');
    return user && user.length ? JSON.parse(user) : null;
  }

  hasRole(expectedRole: string[]): boolean {
    const user = this.getUser() || { authorities: [{ authority: 'ANONYMOUS' }] };
    const authorities = user.authorities || [{ authority: 'ANONYMOUS' }];
    return expectedRole.some(r => authorities.map(a => a.authority.toLowerCase()).includes(r.toLowerCase()));
  }

  getTokenHeader(): { 'x-auth-token': string } {
    const xAuthToken = this.getToken();
    if (xAuthToken && xAuthToken.length) {
      return { 'x-auth-token': `${xAuthToken}` };
    }
  }

  getToken(): string {
    return localStorage.getItem('xAuthToken');
  }

  private setUser(data: User): void {
    localStorage.setItem('user', JSON.stringify(data));
  }
}
