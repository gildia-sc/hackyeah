import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { uniqueValidator } from './unique-validator';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  avatar: string;

  registerForm = this.formBuilder.group({
    login: ['', [Validators.required, Validators.maxLength(50)],
      [uniqueValidator(this.httpClient, '/api/login-taken')]],
    password: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)],
      [uniqueValidator(this.httpClient, '/api/email-taken')]],
    firstName: ['', [Validators.required, Validators.maxLength(50)]],
    lastName: ['', [Validators.required, Validators.maxLength(50)]],
    image: ['']
  });

  constructor(private readonly formBuilder: FormBuilder,
    private readonly httpClient: HttpClient,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar) { }

  ngOnInit() {
  }

  register() {
    this.httpClient.post('/api/register', this.registerForm.value).subscribe(() => {
      this.router.navigate(['/login']);
    }, () => {
      this.snackBar.open('Registration failed', null, {
        duration: 3000
      });
    });
  }

  onFileChange(event) {
    let reader = new FileReader();
    if (event.target.files && event.target.files.length > 0) {
      let file = event.target.files[0];
      reader.readAsDataURL(file);
      reader.onload = () => {
        this.registerForm.controls['image'].setValue('data:' + file.type + ';base64,' + reader.result.split(',')[1])
      };
    }
  }

  triggerAvatarClick() {
    document.querySelector("#avatar").click();
  }

  clearFile() {
    this.registerForm.controls['image'].setValue('')
    this.avatar = null;
  }
}
