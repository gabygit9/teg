import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'difficulty',
  standalone: true
})
export class DifficultyPipe implements PipeTransform {

  transform(difficulty: string): string {
    const difficultyMap: { [key: string]: string } = {
      'Novice': 'Novato',
      'novice': 'Novato',
      'Balanced': 'Balanceado',
      'balanced': 'Balanceado',
      'Expert': 'Experto',
      'expert': 'Experto'
    };
    return difficultyMap[difficulty] || difficulty;
  }

}

