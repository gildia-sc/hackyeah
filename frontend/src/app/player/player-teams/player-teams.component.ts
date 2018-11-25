import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { PlayerService } from '../player.service';
import { PlayerTeamView } from '../view/player-team-view.model';
import { Observable } from "rxjs";
import { TitleService } from '../../title/title.service';

@Component({
  selector: 'app-player-teams',
  templateUrl: './player-teams.component.html',
  styleUrls: ['./player-teams.component.css']
})
export class PlayerTeamsComponent implements OnInit {

    playerTeams: Observable<PlayerTeamView[]>
   
    constructor(
      private playerService: PlayerService,
      private route: ActivatedRoute,
      private location: Location,
      private titleService: TitleService
    ) {}
   
    ngOnInit(): void {
      this.titleService.changeTitle('Player teams');
      const id = +this.route.snapshot.paramMap.get('id');
      this.playerTeams = this.playerService.getPlayerTeams(id)
    }
   
    goBack(): void {
      this.location.back();
    }

}
