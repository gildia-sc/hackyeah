import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { MatchService } from '../../match.service';
import { WebsocketService } from '../../../websocket/websocket.service';
import { Match } from '../../../model/match.model';
import { Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";

@Component({
  selector: 'app-match-list-elem',
  templateUrl: './match-list-element.component.html',
  styleUrls: ['./match-list-element.component.css']
})
export class MatchListElementComponent implements OnInit, OnDestroy {
  private _destroyed$ = new Subject();
  @Input() tableCode: string;

  match$: Subject<Match> = new Subject();

  constructor(private matchService: MatchService,
              private webSocketService: WebsocketService) { }

  ngOnInit() {
    this.matchService.getMatch(this.tableCode)
      .pipe(takeUntil(this._destroyed$))
      .subscribe(match => this.match$.next(match));
    this.subscribeToTableChannel(this.tableCode);
  }

  ngOnDestroy(): void {
    this._destroyed$.next();
    this._destroyed$.complete();
  }

  private subscribeToTableChannel(tableCode: string) {
    this.webSocketService.subscribeToChannel(`${tableCode}`, message => this.match$.next(JSON.parse(message.body)))
  }
}
