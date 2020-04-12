import {FormGroup} from '@angular/forms';

export function MatchPasswordsValidator(group: FormGroup): { differentPasswords: boolean } | null {
  const pass = group.get('password').value;
  const confirmPassword = group.get('confirmPassword').value;
  return pass === confirmPassword ? null : { differentPasswords: true };
}
