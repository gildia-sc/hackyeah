import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { Player } from '../../model/player.model';
import { PlayerView } from '../view/player-view.model';
import { PlayerService } from '../player.service';
import { Observable } from "rxjs";
import { map } from 'rxjs/operators'
import { MatDialog, MatDialogRef, MatSnackBar } from "@angular/material";
import { Router } from "@angular/router";
import { DeleteDialog } from '../../util/delete-dialog/delete-dialog.component'
import { TitleService } from '../../title/title.service';

@Component({
  selector: 'app-player-list',
  templateUrl: './player-list.component.html',
  styleUrls: ['./player-list.component.css']
})
export class PlayerListComponent implements OnInit {

  players: Observable<PlayerView[]>;

  constructor(private readonly playerService: PlayerService,
              private readonly dialog: MatDialog,
              private readonly router: Router,
              private readonly snackBar: MatSnackBar,
              private readonly titleService: TitleService) { }

  ngOnInit() {
    this.titleService.changeTitle('Players');
    this.players = this.playerService.getAllPlayers();
  }

  toggleExpansionPanel(id: number) {
    let allExpansionBodies: HTMLElement[] = [].slice.call(document.querySelectorAll(".expansion-body"));
    let expansionBody: HTMLElement = document.querySelector("#expansion-body-" + id) as HTMLElement;
    if (!expansionBody.classList.contains("hide")) {
      expansionBody.classList.add("hide");
    } else {
      allExpansionBodies.forEach(item => item.classList.add("hide"));
      expansionBody.classList.remove("hide");
    }
  }

  openDeleteDialog(id: number): void {
    const dialogRef = this.dialog.open(DeleteDialog, {
      width: '250px'
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
      if (result) {
        this.playerService.deletePlayer(id).subscribe(() => {
          this.players = this.playerService.getAllPlayers()
        }, () => {
          this.snackBar.open('Update failed', null, {
            duration: 3000
          });
        });
      }
    });
  }
}
