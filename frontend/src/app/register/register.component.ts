import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { uniqueValidator } from './unique-validator';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material';
import { TitleService } from '../title/title.service';
import { ImageCropperComponent, CropperSettings } from 'ng2-img-cropper';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  avatar: any;
  id: number;
  cropperSettings: CropperSettings;

  @ViewChild('avatarInput', undefined)
  avatarInput: ImageCropperComponent;

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
    private readonly snackBar: MatSnackBar,
    private readonly titleService: TitleService) {

    this.cropperSettings = new CropperSettings();
    this.cropperSettings.width = 100;
    this.cropperSettings.height = 100;
    this.cropperSettings.croppedWidth = 100;
    this.cropperSettings.croppedHeight = 100;
    this.cropperSettings.canvasWidth = 400;
    this.cropperSettings.canvasHeight = 300;
    this.cropperSettings.rounded = true;
    this.cropperSettings.noFileInput = true;

    this.avatar = {};
  }

  ngOnInit() {
    this.titleService.changeTitle('Register');
  }

  register() {
    this.registerForm.controls['image'].setValue(this.avatar.image)
    this.httpClient.post('/api/register', this.registerForm.value).subscribe(() => {
      this.router.navigate(['/login']);
    }, () => {
      this.snackBar.open('Registration failed', null, {
        duration: 3000
      });
    });
  }

  fileChangeListener($event) {
    var image: any = new Image();
    var file: File = $event.target.files[0];
    var myReader: FileReader = new FileReader();
    var that = this;
    myReader.onloadend = function (loadEvent: any) {
      image.src = loadEvent.target.result;
      that.avatarInput.setImage(image);
      that.avatar.image = image;
    };

    myReader.readAsDataURL(file);
  }
  
  triggerAvatarClick() {
    document.querySelector("#avatar").classList.remove("hide");
    (document.querySelector("#avatarInput") as HTMLElement).click();
  }

  clearFile() {
    this.registerForm.controls['image'].setValue('')
    this.avatar.image = null;
    document.querySelector("#avatar").classList.add("hide");
    this.avatarInput.setImage(null);
  }
}
