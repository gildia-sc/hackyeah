import { Player } from "./player.model";
import { Team } from "./team.model";

export class Match {
    id: number;
    startTime: string;
    endTime: string;
    playerAlphaAttacker: Player;
    playerAlphaGoalkeeper: Player;
    playerBetaAttacker: Player;
    playerBetaGoalkeeper: Player;
    teamAlpha: Team;
    teamBeta: Team;
    playerAlphaAttackerScore: number;
    playerBetaAttackerScore: number;
    playerAlphaGoalkeeperScore: number;
    playerBetaGoalkeeperScore: number;
    teamAlphaScore: number;
    teamBetaScore: number;
}
