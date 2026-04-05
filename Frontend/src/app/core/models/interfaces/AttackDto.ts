export interface AttackDto {
  gameId: number;
 // jugadorPartidaId: number;
  countryIdAttacker: number;
  countryIdDefensor: number;
  dice: number;
}

export interface AttackResultDto {
  diceAttacker: number[];
  diceDefensor: number[];
  wereConquered: boolean;
}
