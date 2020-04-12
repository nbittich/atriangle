import {FormGroup} from '@angular/forms';

export function PasswordValidator(group: FormGroup): { sameAsOldPassword: boolean } | null {
  const oldPassword = group.get('oldPassword').value;
  const password = group.get('password').value;

  return oldPassword === password ? { sameAsOldPassword: true } : null;
}
