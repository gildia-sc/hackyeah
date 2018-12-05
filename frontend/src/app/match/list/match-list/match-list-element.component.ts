import { Component, Input, OnInit } from '@angular/core';
import { MatchService } from '../../match.service';
import { WebsocketService } from '../../../websocket/websocket.service';
import { Match } from '../../../model/match.model';

@Component({
  selector: 'app-match-list-elem',
  templateUrl: './match-list-element.component.html',
  styleUrls: ['./match-list-element.component.css']
})
export class MatchListElementComponent implements OnInit {
  @Input() tableCode: string;

  match: Match;

  constructor(private matchService: MatchService,
              private webSocketService: WebsocketService) { }

  ngOnInit() {
    this.matchService.getMatch(this.tableCode)
      .subscribe(match => {
        if (match) {
          this.match = match;
        }
      });
    this.subscribeToTableChannel(this.tableCode);
  }

  private subscribeToTableChannel(tableCode: string) {
    this.webSocketService.subscribeToChannel(`${tableCode}`, message => {
      if (message.body) {
        this.match = JSON.parse(message.body) as Match;
      }
    })
  }

  get alphaScore(): number {
    return this.match != null ? this.match.alphaScore : 0;
  }

  get betaScore(): number {
    return this.match != null ? this.match.betaScore : 0;
  }

  get alphaColor(): string {
    return this.match != null ? this.match.alphaColor : 'blue';
  }

  get betaColor(): string {
    return this.match != null ? this.match.betaColor : 'red';
  }
}
