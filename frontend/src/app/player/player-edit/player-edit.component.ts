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

@Component({
  selector: 'app-player-edit',
  templateUrl: './player-edit.component.html',
  styleUrls: ['./player-edit.component.css']
})
export class PlayerEditComponent implements OnInit {
  avatar: string;

  id: number;

  updateForm = this.formBuilder.group({
    login: [''],
    email: [''],
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
    private playerService: PlayerService) { }
 
  ngOnInit(){
    const id = +this.route.snapshot.paramMap.get('id');
    this.playerService.getPlayer(id).subscribe(item => this.fillUpdateForm(item));
  }

  fillUpdateForm(player: Player) {
    this.id = player.id
    this.updateForm.controls['login'].setValue(player.login)
    // this.updateForm.controls['login'].setValidators([Validators.required, Validators.maxLength(50), 
    //   uniqueValidator(this.httpClient, `/api/players/${this.id}/login-taken`)])
    this.updateForm.controls['email'].setValue(player.email)
    // this.updateForm.controls['email'].setValidators([Validators.required, Validators.email, Validators.maxLength(254),
    //   uniqueValidator(this.httpClient, `/api/players/${this.id}/email-taken`)])
    this.updateForm.controls['firstName'].setValue(player.firstName)
    this.updateForm.controls['lastName'].setValue(player.lastName)
    this.updateForm.controls['image'].setValue(player.image)
  }

  getPlayerFromForm(): Player {
    return new Player(
      this.id,
      this.updateForm.get('login').value,
      this.updateForm.get('email').value,
      this.updateForm.get('firstName').value,
      this.updateForm.get('lastName').value,
      this.updateForm.get('image').value
    );
  }

  onCancelClick(): void {
    this.location.back();
  }

  update() {
    this.playerService.updatePlayer(this.getPlayerFromForm()).subscribe(() => {
      this.router.navigate(['/players']);
    });
  }

  onFileChange(event) {
    let reader = new FileReader();
    if (event.target.files && event.target.files.length > 0) {
      let file = event.target.files[0];
      reader.readAsDataURL(file);
      reader.onload = () => {
        this.updateForm.controls['image'].setValue(reader.result as string)
      };
    }
  }

  triggerAvatarClick() {
    let avatarInput: HTMLElement = document.querySelector("#avatar") as HTMLElement;
    avatarInput.click();
  }

  clearFile() {
    this.updateForm.controls['image'].setValue('')
    this.avatar = null;
  }

}
