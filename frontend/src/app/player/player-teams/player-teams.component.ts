import { Component, OnInit } from '@angular/core';
import { Team } from '../../model/team.model'

@Component({
  selector: 'app-player-teams',
  templateUrl: './player-teams.component.html',
  styleUrls: ['./player-teams.component.css']
})
export class PlayerTeamsComponent implements OnInit {

  teams: Team[] = []

  constructor() { }

  ngOnInit() {
  }

}
