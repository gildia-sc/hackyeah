import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatchService } from '../match.service';
import { ActivatedRoute } from '@angular/router';
import { WebsocketService } from '../../websocket/websocket.service';
import { MatSnackBar } from '@angular/material';
import { TitleService } from '../../title/title.service';
import { Match } from '../../model/match.model';
import { map, switchMap, takeUntil } from "rxjs/operators";
import { Observable, Subject } from "rxjs";

@Component({
  selector: 'app-table',
  templateUrl: './match.component.html',
  styleUrls: ['./match.component.css'],
})
export class MatchComponent implements OnInit, OnDestroy {
  private _destroyed$ = new Subject();
  match$: Subject<Match> = new Subject();

  constructor(private matchService: MatchService,
              private route: ActivatedRoute,
              private webSocketService: WebsocketService,
              private snackBar: MatSnackBar,
              private titleService: TitleService) {
  }

  scoreGoal(match: Match, team: string, position?: string) {
    if (match.startTime && !match.endTime) {
      this.matchService.scoreGoal(match.tableCode, team, position).subscribe();
    }
  }

  takePosition(match: Match, team: string, position: string) {
    if (!match.startTime) {
      this.matchService.takePosition(match.tableCode, team, position).subscribe();
    }
  }

  freePositionOrScoreGoal(match: Match, team: string, position: string) {
    if (!match.startTime) {
      this.matchService.freePosition(match.tableCode, team, position).subscribe();
    }

    if (match.startTime && !match.endTime) {
      this.matchService.scoreGoal(match.tableCode, team, position).subscribe();
    }
  }

  switchPositions(match: Match, team: string) {
    if (!match.endTime) {
      this.matchService.switchPositions(match.tableCode, team).subscribe();
    }
  }

  ngOnInit() {
    this.route.params.pipe(
      map(params => {
        return {
          code: params['tableCode'],
          team: params['side'],
          position: params['role']
        }
      }),
      switchMap(params => this.getMatch(params)),
      takeUntil(this._destroyed$)
    ).subscribe(match => {
      this.match$.next(match);
      this.subscribeToTableChannel(match.tableCode);
      this.titleService.changeTitle(`Match ${match.tableCode}`);
    });
  }

  ngOnDestroy(): void {
    this._destroyed$.next();
    this._destroyed$.complete();
  }

  private getMatch(params): Observable<Match> {
    if (params.team && params.position) {
      return this.matchService.takePosition(params.code, params.team, params.position);
    }
    return this.matchService.getMatch(params.code);
  }

  subscribeToTableChannel(tableCode: string) {
    this.webSocketService.subscribeToChannel(`${tableCode}`, message => {
      const match = JSON.parse(message.body) as Match;
      this.match$.next(match);
    });
  }

}
