import { Component, OnInit } from '@angular/core';
import { PlayerMatchView } from '../view/player-match-view.model';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { PlayerService } from '../player.service';
import { Observable } from "rxjs";

@Component({
  selector: 'app-player-matches',
  templateUrl: './player-matches.component.html',
  styleUrls: ['./player-matches.component.css']
})
export class PlayerMatchesComponent implements OnInit {

  playerMatches: Observable<PlayerMatchView[]>
   
  constructor(
    private playerService: PlayerService,
    private route: ActivatedRoute,
    private location: Location
  ) {}p
 
  ngOnInit(): void {
    const id = +this.route.snapshot.paramMap.get('id');
    this.playerMatches = this.playerService.getPlayerMatches(id)
  }
 
  goBack(): void {
    this.location.back();
  }
}
