import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { Player } from '../../model/player.model';
import { PlayerView } from '../view/player-view.model';
import { PlayerService } from '../player.service';
import { Observable } from "rxjs";
import { map } from 'rxjs/operators'
import {MatDialog, MatDialogRef, MatSnackBar} from "@angular/material";
import {Router} from "@angular/router";
import {DeleteDialog} from '../../util/delete-dialog/delete-dialog.component'

@Component({
  selector: 'app-player-list',
  templateUrl: './player-list.component.html',
  styleUrls: ['./player-list.component.css']
})
export class PlayerListComponent implements OnInit {

  players: Observable<PlayerView[]>;

  constructor(private playerService: PlayerService,
              private dialog: MatDialog,
              private router: Router,
              private snackBar: MatSnackBar) { }

  ngOnInit() {
    this.players = this.playerService.getAllPlayers();
  }
  
  openDeleteDialog(id: number): void {
    const dialogRef = this.dialog.open(DeleteDialog, {
      width: '250px'
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
      if(result) {
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
