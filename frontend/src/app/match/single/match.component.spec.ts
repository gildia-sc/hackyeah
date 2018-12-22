import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MatchComponent } from './match.component';
import { MatchNotificationsComponent } from "../match-notifications/match-notifications.component";
import { MatChipsModule, MatIconModule, MatSnackBarModule } from "@angular/material";
import { TableImageComponent } from "../table-image/table-image.component";
import { TimerComponent } from "../timer/timer.component";
import { MatchService } from "../match.service";
import { WebsocketService } from "../../websocket/websocket.service";
import { TitleService } from "../../title/title.service";
import { ActivatedRoute } from "@angular/router";
import { from, of } from "rxjs";
import { Match } from "../../model/match.model";
import { Player } from "../../model/player.model";

fdescribe('MatchComponent', () => {
  let matchService: jasmine.SpyObj<MatchService>;
  let webSocketService: jasmine.SpyObj<WebsocketService>;
  let titleService: jasmine.SpyObj<TitleService>;

  let component: MatchComponent;
  let fixture: ComponentFixture<MatchComponent>;

  beforeEach(async(() => {
    matchService = jasmine.createSpyObj('MatchService', ['getMatch', 'scoreGoal', 'takePosition']);
    webSocketService = jasmine.createSpyObj('WebsocketService', ['subscribeToChannel']);
    titleService = jasmine.createSpyObj('TitleService', ['changeTitle']);

    TestBed.configureTestingModule({
      imports: [
        MatChipsModule,
        MatIconModule,
        MatSnackBarModule
      ],
      declarations: [MatchComponent, MatchNotificationsComponent, TableImageComponent, TimerComponent],
      providers: [
        {provide: MatchService, useValue: matchService},
        {provide: WebsocketService, useValue: webSocketService},
        {provide: TitleService, useValue: titleService},
        {provide: ActivatedRoute, useValue: {'params': from([{'tableCode': 'F16'}])}}
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    matchService.getMatch.and.returnValue(of(getMatchData()));
    matchService.scoreGoal.and.returnValue(of(getMatchData()));
    matchService.takePosition.and.returnValue(of(getMatchData()));

    fixture = TestBed.createComponent(MatchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch a match if tableCode param is present in the route', () => {
    expect(matchService.getMatch).toHaveBeenCalledWith('F16');
    expect(titleService.changeTitle).toHaveBeenCalledWith('Match F16');
    expect(webSocketService.subscribeToChannel).toHaveBeenCalledTimes(1);
  });

  it('should not call service scoreGoal when match is no started', () => {
    const newMatch: Match = getMatchData();
    component.scoreGoal(newMatch, 'red', 'attacker');
    expect(matchService.scoreGoal).toHaveBeenCalledTimes(0);
  });

  it('should not call service scoreGoal when match has ended', () => {
    const match: Match = getMatchData();
    match.endTime = new Date();
    component.scoreGoal(match, 'red', 'attacker');
    expect(matchService.scoreGoal).toHaveBeenCalledTimes(0);
  });

  it('should call service scoreGoal, when match has started', () => {
    const match: Match = getMatchData();
    match.startTime = new Date();

    component.scoreGoal(match, 'red');
    expect(matchService.scoreGoal).toHaveBeenCalledWith('F16', 'red', undefined);

    component.scoreGoal(match, 'red', 'attacker');
    expect(matchService.scoreGoal).toHaveBeenCalledWith('F16', 'red', 'attacker')
  });

  it('should not call service takePosition, when the match has started', () => {
    const match: Match = getMatchData();
    match.startTime = new Date();

    component.takePosition(match, 'blue', 'attacker');
    expect(matchService.takePosition).toHaveBeenCalledTimes(0);
  });

  it('should call service takePosition, when the match has not started', () => {
    const match: Match = getMatchData();

    component.takePosition(match, 'blue', 'goalkeeper');
    expect(matchService.takePosition).toHaveBeenCalledWith('F16', 'blue', 'goalkeeper');
  });

  function getMatchData(): Match {
    return {
      tableCode: "F16",
      alphaAttacker: {
        id: 4,
        name: "User User",
        image: ""
      } as Player,
      alphaGoalkeeper: null,
      betaAttacker: null,
      betaGoalkeeper: null,
      alphaColor: "#8080ff",
      betaColor: "#f5f5f5",
      started: false,
      startTime: null,
      endTime: null,
      alphaScore: 0,
      betaScore: 0,
      reservationStart: "2018-12-27T10:50:42.286"
    }
  }
});
