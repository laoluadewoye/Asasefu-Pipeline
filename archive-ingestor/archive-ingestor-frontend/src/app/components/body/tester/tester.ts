import { Component, inject, signal, Input, OnInit } from '@angular/core';
import { ArchiveServerTestService } from '../../../services/archive-server-test';
import { ArchiveServerTestData } from '../../../models/archive-server-test-data';
import { catchError } from 'rxjs';

@Component({
  selector: 'app-tester',
  imports: [],
  templateUrl: './tester.html',
  styleUrl: './tester.css',
})
export class Tester implements OnInit {
    @Input() recievedDefaultValue!: string;

    archiveServerTestService: ArchiveServerTestService = inject(ArchiveServerTestService);
    archiveServerTestData = signal<ArchiveServerTestData>({testData: ""});
    buttonText: string = "Test API";
    buttonPressed = signal(false);

    ngOnInit(): void {
        this.archiveServerTestData.set({testData: this.recievedDefaultValue});
    }

    onTesterButtonClick() {
        this.archiveServerTestService.getArchiveServerTestData().pipe(
            catchError((err) => {
                console.log(err);
                throw err;
            })
        ).subscribe((result) => {
            this.archiveServerTestData.set(result);
        })

        this.buttonPressed.set(true);

        setTimeout(() => {
            this.buttonPressed.set(false);
            this.archiveServerTestData.set({testData: this.recievedDefaultValue});
        }, 5000);
    }
}
