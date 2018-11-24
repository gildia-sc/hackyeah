import { Component, OnInit } from '@angular/core';
import { Player } from "../../model/player.model";
import { Match, MatchService } from "../match.service";
import { ActivatedRoute } from "@angular/router";

@Component({
  selector: 'app-table',
  templateUrl: './match.component.html',
  styleUrls: ['./match.component.css']
})
export class MatchComponent implements OnInit {
  private tableCode: string;

  match: Match;

  timerSeconds = 60;
  matchStarted = true;

  alphaScore = 0;
  betaScore = 0;

  constructor(private matchService: MatchService,
              private route: ActivatedRoute) {
  }

  incScore(team: string, position?: string) {
    if (team === 'alpha') {
      this.match.alphaScore++;
    }

    if (team === 'beta') {
      this.match.betaScore++;
    }

    this.matchService.scoreGoal(this.tableCode, team, position)
      .subscribe(match => {
        if (match) {
          this.match = match;
        }
      });
  }

  takePosition(team: string, position: string) {
    this.matchService.takePosition(this.tableCode, team, position)
      .subscribe(match => {
        if (match) {
          this.match = match;
        }
      })
  }

  freePosition(team: string, position: string) {
    this.matchService.freePosition(this.tableCode, team, position)
      .subscribe(match => {
        if (match) {
          this.match = match;
        }
      })
  }

  switchPositions(team: string) {
    this.matchService.switchPositions(this.tableCode, team)
      .subscribe(match => {
          if (match) {
            this.match = match;
          }
        }
      )
  }

  private clearCurrentPosition(player: Player) {

  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.tableCode = params['tableCode'];
      this.matchService.getMatch(this.tableCode).subscribe(match => {
        if (match) {
          this.match = match;
        }
      })
    })
  }

  private isPositionFree(team: string, position: string) {
    return true;
  }
}
