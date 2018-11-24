import { Component, OnInit } from '@angular/core';
import { PlayerMatchView } from '../view/player-match-view.model';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { PlayerViewService } from '../player.service';
import { Observable } from "rxjs";

@Component({
  selector: 'app-player-matches',
  templateUrl: './player-matches.component.html',
  styleUrls: ['./player-matches.component.css']
})
export class PlayerMatchesComponent implements OnInit {

  playerMatches: Observable<PlayerMatchView[]>
   
  constructor(
    private playerViewService: PlayerViewService,
    private route: ActivatedRoute,
    private location: Location
  ) {}
 
  ngOnInit(): void {
    const id = +this.route.snapshot.paramMap.get('id');
    this.playerMatches = this.playerViewService.getPlayerMatches(id)
  }
 
  goBack(): void {
    this.location.back();
  }
}
