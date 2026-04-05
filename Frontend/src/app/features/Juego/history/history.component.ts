import { Component, Input, OnInit } from '@angular/core';
import { CommonModule, DatePipe, NgForOf, NgIf } from '@angular/common';
import { EventHistory } from '../../../core/models/interfaces/EventHistory';
import { HistoryService } from '../../../core/services/history.service';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [
    CommonModule
  ],
  providers: [DatePipe],
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.css']
})
export class HistoryComponent implements OnInit {
  @Input() gameId!: number;
  history: EventHistory[] = [];
  openHistory: boolean = true;


  constructor(private historyService: HistoryService, private datePipe: DatePipe) {}

  ngOnInit(): void {
    this.loadHistory()
  }


  loadHistory(): void {
    if (this.gameId) {
      this.historyService.getHistory(this.gameId).subscribe(data => {
        this.history = data;
      });
    } else {
      console.warn("No se recibió partidaId");
    }
  }

  reloadHistory(): void {
    this.loadHistory();
  }

  getHour(date: string): string {
    return this.datePipe.transform(date, 'HH:mm') || '';
  }

  closeHistory(): void {
    this.openHistory = false;
  }
}

