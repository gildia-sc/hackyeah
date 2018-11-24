import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { Player } from '../../model/player.model';
import { PlayerView } from '../view/player-view.model';
import { PlayerViewService } from '../player.service';
import { Observable } from "rxjs";
import { map } from 'rxjs/operators'

@Component({
  selector: 'app-player-list',
  templateUrl: './player-list.component.html',
  styleUrls: ['./player-list.component.css']
})
export class PlayerListComponent implements OnInit {

  players: Observable<PlayerView[]>;

  constructor(private playerService: PlayerViewService) { }

  ngOnInit() {
    this.players = this.playerService.getAllPlayers();
  }

}
