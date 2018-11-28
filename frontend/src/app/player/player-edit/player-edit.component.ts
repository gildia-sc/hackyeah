import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { uniqueValidator } from '../../register/unique-validator';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { PlayerService } from '../player.service';
import { Player } from 'src/app/model/player.model';
import { Observable } from 'rxjs';
import { TitleService } from '../../title/title.service';
import { ImageCropperComponent, CropperSettings } from 'ng2-img-cropper';

@Component({
  selector: 'app-player-edit',
  templateUrl: './player-edit.component.html',
  styleUrls: ['./player-edit.component.css']
})
export class PlayerEditComponent implements OnInit {
  avatar: any;
  id: number;
  cropperSettings: CropperSettings;

  @ViewChild('avatarInput', undefined)
  avatarInput: ImageCropperComponent;

  updateForm = this.formBuilder.group({
    login: ['', [Validators.required, Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
    firstName: ['', [Validators.required, Validators.maxLength(50)]],
    lastName: ['', [Validators.required, Validators.maxLength(50)]],
    image: ['']
  });

  constructor(private readonly formBuilder: FormBuilder,
    private readonly httpClient: HttpClient,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private location: Location,
    private playerService: PlayerService,
    private titleService: TitleService) {

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
    this.titleService.changeTitle('Edit player');
    const id = +this.route.snapshot.paramMap.get('id');
    this.playerService.getPlayer(id).subscribe(item => this.fillUpdateForm(item));
  }

  fillUpdateForm(player: Player) {
    this.id = player.id
    this.updateForm.controls['login'].setValue(player.login)
    this.updateForm.controls['login'].setAsyncValidators(uniqueValidator(this.httpClient, `/api/players/${this.id}/login-taken`))
    this.updateForm.controls['email'].setValue(player.email)
    this.updateForm.controls['email'].setAsyncValidators(uniqueValidator(this.httpClient, `/api/players/${this.id}/email-taken`))
    this.updateForm.controls['firstName'].setValue(player.firstName)
    this.updateForm.controls['lastName'].setValue(player.lastName)
    this.updateForm.controls['image'].setValue(player.image)
    this.avatar.image = player.image;
  }

  getPlayerFromForm(): Player {
    return new
      Player(
        this.id,
        this.updateForm.get('login').value,
        this.updateForm.get('email').value,
        this.updateForm.get('firstName').value,
        this.updateForm.get('lastName').value,
        this.updateForm.get('image').value
      );
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

  onCancelClick(): void {
    this.router.navigate(['../'], { relativeTo: this.route });
  }

  update() {
    this.updateForm.controls['image'].setValue(this.avatar.image)
    this.playerService.updatePlayer(this.getPlayerFromForm()).subscribe(() => {
      this.router.navigate(['/players']);
    });
  }

  triggerAvatarClick() {
    document.querySelector("#avatar").classList.remove("hide");
    (document.querySelector("#avatarInput") as HTMLElement).click();
  }

  clearFile() {
    this.updateForm.controls['image'].setValue('')
    this.avatar.image = null;
    document.querySelector("#avatar").classList.add("hide");
    this.avatarInput.setImage(null);
  }

}
