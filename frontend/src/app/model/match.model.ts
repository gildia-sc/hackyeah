import { Player } from './player.model';

export class Match {
  tableCode: string;
  alphaAttacker: Player;
  alphaGoalkeeper: Player;
  betaAttacker: Player;
  betaGoalkeeper: Player;
  alphaColor: string;
  betaColor: string;
  started: boolean;
  alphaScore: number;
  betaScore: number;
  startTime: string | Date;
  endTime: string | Date;
  reservationStart: string | Date
}
