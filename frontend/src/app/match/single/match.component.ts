import { Component, OnInit } from '@angular/core';
import { Match, MatchService } from "../match.service";
import { ActivatedRoute } from "@angular/router";
import { WebsocketService } from "../../websocket/websocket.service";
import { MatSnackBar } from "@angular/material";

@Component({
  selector: 'app-table',
  templateUrl: './match.component.html',
  styleUrls: ['./match.component.css']
})
export class MatchComponent implements OnInit {
  private tableCode: string;

  match: Match;

  constructor(private matchService: MatchService,
              private route: ActivatedRoute,
              private webSocketService: WebsocketService,
              private snackBar: MatSnackBar) {
  }

  scoreGoal(team: string, position?: string) {
    if (this.matchStarted && !this.matchEnded) {
      this.matchService.scoreGoal(this.tableCode, team, position).subscribe();
    }
  }

  takePosition(team: string, position: string) {
    if (!this.matchStarted) {
      this.matchService.takePosition(this.tableCode, team, position).subscribe()
    }
  }

  freePosition(team: string, position: string) {
    if (!this.matchStarted) {
      this.matchService.freePosition(this.tableCode, team, position).subscribe()
    }
  }

  switchPositions(team: string) {
    if (!this.matchStarted) {
      this.matchService.switchPositions(this.tableCode, team).subscribe()
    }
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.tableCode = params['tableCode'];
      this.subscribeToTableChannel(this.tableCode);
      this.matchService.getMatch(this.tableCode).subscribe(match => {
        if (match) {
          this.match = match;
          let side = params['side'];
          let role = params['role'];
          if(side && role) {
            console.log(`Enter with side ${side} and ${role}`)
            if (!this.matchStarted) {
              this.matchService.takePosition(this.tableCode, side, role).subscribe(() => {
                this.snackBar.open('Position taken', null, {
                  duration: 3000
                });
              })
            } else {
              this.snackBar.open('Match already started, position has not taken', null, {
                duration: 3000
              });
            }
          } else {
            console.log('Enter without side or role')
          }
        }
      });
    })
  }

  subscribeToTableChannel(tableCode: string) {
    this.webSocketService.subscribeToChannel(`${tableCode}`, message => {
      if (message.body) {
        this.match = JSON.parse(message.body) as Match;
        if (this.matchEnded) {
          this.displayDisplayWinner();
        }

      }
    })
  }

  get alphaScore(): number {
    return this.match != null ? this.match.alphaScore : 0;
  }

  get betaScore(): number {
    return this.match != null ? this.match.betaScore : 0;
  }

  get matchStarted() {
    return this.match != null ? this.match.started : false;
  }

  get matchEnded() {
    return this.match != null ? this.match.endTime != null : false;
  }

  private resetTable() {
    this.match = null;
  }

  private displayDisplayWinner() {
    let winner;

    if (this.alphaScore > this.betaScore) {
      winner = 'Alpha'
    } else {
      winner = 'Beta'
    }

    this.snackBar.open(`The match has ended. Team ${winner} has won the match! Final result is ${this.alphaScore} : ${this.betaScore}.`,
      null, {duration: 5000}
    )
      .afterDismissed()
      .subscribe(() => this.resetTable());

  }
}
