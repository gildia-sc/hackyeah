import { Player } from "./player.model";

export class Team {
    id: number;
    name: string;
    attacker: Player;
    goalkeeper: Player;
    createdTime: string;
    closeTime: string;
}