import { Component, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  registerForm = this.formBuilder.group({
    login: [''],
    password: [''],
    email: [''],
    firstName: [''],
    lastName: [''],
  });

  constructor(private readonly formBuilder: FormBuilder,
              private readonly httpClient: HttpClient) { }

  ngOnInit() {
  }

  register() {
    this.httpClient.post('/api/register', {}).subscribe(() => {

    });
  }
}
