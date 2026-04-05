export interface GameConfigForm{
  players: number;
  bots: Bot[];
  host: Player;
}

export interface Bot {
  id: number;
  name: string;
  difficulty: string;
  color: Color;
}

export interface Color {
  id: number;
  name: string;
}

export interface Player {
  name: string | null;
  color: Color;
}
