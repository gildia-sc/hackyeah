import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { PlayerViewService } from '../player.service';
import { PlayerTeamView } from '../view/player-team-view.model';
import { Observable } from "rxjs";

@Component({
  selector: 'app-player-teams',
  templateUrl: './player-teams.component.html',
  styleUrls: ['./player-teams.component.css']
})
export class PlayerTeamsComponent implements OnInit {

    playerTeams: Observable<PlayerTeamView[]>
   
    constructor(
      private playerViewService: PlayerViewService,
      private route: ActivatedRoute,
      private location: Location
    ) {}
   
    ngOnInit(): void {
      const id = +this.route.snapshot.paramMap.get('id');
      this.playerTeams = this.playerViewService.getPlayerTeams(id)
    }
   
    goBack(): void {
      this.location.back();
    }

}
