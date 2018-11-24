import { Component, OnInit } from '@angular/core';
import { Player } from "../model/player.model";

@Component({
  selector: 'app-table',
  templateUrl: './single-game.component.html',
  styleUrls: ['./single-game.component.css']
})
export class SingleGameComponent implements OnInit {
  private config = {
    maxScoreBound: 10
  };

  private takenCount = 0;

  positions: Map<string, Position> = new Map<string, Position>(
    [
      ['teamAlphaAttacker', new Position()],
      ['teamBetaAttacker', new Position()],
      ['teamAlphaGoalkeeper', new Position()],
      ['teamBetaGoalkeeper', new Position()],
    ],
  );

  timerSeconds = 60;
  teamAlphaScore = 0;
  teamBetaScore = 0;
  matchStarted = true;

  currentPlayer: Player = {
    id: 123,
    name: 'Tomek'
  };

  constructor() {
  }

  incScore(side: string, player?: string) {
    if (side === 'alpha') {
      this.teamAlphaScore++;
    }

    if (side === 'beta') {
      this.teamBetaScore++;
    }

    if (this.teamAlphaScore === this.config.maxScoreBound
      || this.teamBetaScore === this.config.maxScoreBound) {
      //  zakończ mecz, zablokuj przyciski
    }

  }

  resetScore() {
    this.teamAlphaScore = 0;
    this.teamBetaScore = 0;
  }

  takePosition(name: string, player: Player) {
    const position = this.positions.get(name);
    if (!position.taken) {
      this.clearCurrentPosition(player);
      position.taken = true;
      position.player = player;
      this.takenCount++;

      if (this.allTaken()) {
        alert('ALL TAKEN');
      }

    }
  }

  freePosition(name: string) {
    const position = this.positions.get(name);
    position.taken = false;
    position.player = null;
    this.takenCount--;
  }

  switchPositions(name: string) {
    if (name === 'alpha') {
      const attacker = this.positions.get('teamAlphaAttacker');
      this.positions.set('teamAlphaAttacker', this.positions.get('teamAlphaGoalkeeper'));
      this.positions.set('teamAlphaGoalkeeper', attacker);
    }

    if (name === 'beta') {
      const attacker = this.positions.get('teamBetaAttacker');
      this.positions.set('teamBetaAttacker', this.positions.get('teamBetaGoalkeeper'));
      this.positions.set('teamBetaGoalkeeper', attacker);
    }
  }

  private clearCurrentPosition(player: Player) {
    const currentPosition = this.findPositionByPlayer(player);
    if (currentPosition) {
      currentPosition.taken = false;
      currentPosition.player = null;
      this.takenCount--;
    }
  }

  private findPositionByPlayer(player: Player): Position {
    let positionTaken = null;
    this.positions.forEach((position, posName) => {
      if (position.taken && position.player.id === player.id) {
        positionTaken = position;
      }
    });
    return positionTaken;
  }

  ngOnInit() {
  }

  private allTaken() {
    return this.takenCount === 4;
  }
}

class Position {
  taken: boolean;
  player?: Player;
}
