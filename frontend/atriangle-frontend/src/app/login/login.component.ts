import { Component, OnInit } from '@angular/core';
import {AbstractControl, FormControl, FormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../core/service/auth.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm: FormGroup;
  private message: string;
  private status?: number;

  isVisible: boolean = false;
  constructor(private actRoute: ActivatedRoute, private router: Router, private authenticationService: AuthService) {
    this.loginForm = this.createFormGroup();
  }

  ngOnInit() {
  }
  get email(): AbstractControl {
    return this.loginForm.get('email');
  }

  get password(): AbstractControl {
    return this.loginForm.get('password');
  }

  login(): void {
    const emailValue = this.email.value;
    const passwordValue = this.password.value;
    this.authenticationService
      .login(emailValue, passwordValue)
      .subscribe(
        () => {
          this.reset();
          this.router.navigateByUrl('/');
        },
        () => {
          this.message = 'username/password not found';
          this.isVisible = true;
          setTimeout(() => (this.isVisible = false), 5000);
        }
      );
  }

  getMessage(): string {
    return this.message;
  }

  private createFormGroup(): FormGroup {
    return new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, Validators.minLength(4)])
    });
  }

  private reset(): void {
    this.message = null;
    this.status = null;
    this.loginForm.reset();
  }
}
