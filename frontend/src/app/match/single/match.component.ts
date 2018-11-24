import { Component, OnInit } from '@angular/core';
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
  alphaScore = 0;
  betaScore = 0;

  matchStarted = false;
  matchEnded = false;

  constructor(private matchService: MatchService,
              private route: ActivatedRoute) {
  }

  scoreGoal(team: string, position?: string) {
    if (this.matchStarted && !this.matchEnded) {
      this.matchService.scoreGoal(this.tableCode, team, position)
        .subscribe(match => {
          if (match) {
            this.alphaScore = match.alphaScore;
            this.betaScore = match.betaScore;
          }
        });
    }
  }

  takePosition(team: string, position: string) {
    if (!this.matchStarted) {
      this.matchService.takePosition(this.tableCode, team, position)
        .subscribe(match => {
          if (match) {
            this.match = match;
            this.matchStarted = match.started;
          }
        })
    }
  }

  freePosition(team: string, position: string) {
    if (!this.matchStarted) {
      this.matchService.freePosition(this.tableCode, team, position)
        .subscribe(match => {
          if (match) {
            this.match = match;
          }
        })
    }
  }

  switchPositions(team: string) {
    if (!this.matchStarted) {
      this.matchService.switchPositions(this.tableCode, team)
        .subscribe(match => {
            if (match) {
              this.match = match;
            }
          }
        )
    }
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.tableCode = params['tableCode'];
      this.matchService.getMatch(this.tableCode).subscribe(match => {
        if (match) {
          this.match = match;
          this.alphaScore = match.alphaScore;
          this.betaScore = match.betaScore;
          this.matchStarted = match.started;
        }
      })
    })
  }

}
