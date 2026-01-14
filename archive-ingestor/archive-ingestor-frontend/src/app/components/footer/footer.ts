import { Component, inject, input, InputSignal, OnInit, signal, WritableSignal } from '@angular/core';
import { ArchiveServerSpecService } from '../../services/archive-server-spec';
import { ArchiveServerSpecData } from '../../models/archive-server-spec-data';
import { catchError, of } from 'rxjs';

@Component({
  selector: 'app-footer',
  imports: [],
  templateUrl: './footer.html',
  styleUrl: './footer.css',
})
export class Footer implements OnInit {
    parentDefaultValue: InputSignal<string> = input.required<string>();
    
    archiveServerSpecService: ArchiveServerSpecService = inject(ArchiveServerSpecService);
    archiveServerSpecData: WritableSignal<ArchiveServerSpecData> = signal<ArchiveServerSpecData>({
        archiveIngestorVersion: "", latestOTWArchiveVersion: ""
    });

    ngOnInit(): void {
        // Set default value
        this.archiveServerSpecData.set({
            archiveIngestorVersion: this.parentDefaultValue(), 
            latestOTWArchiveVersion: this.parentDefaultValue()
        });

        // Set actual value
        this.archiveServerSpecService.getArchiveServerSpecData().pipe(
            catchError((err) => {
                return of(new ArchiveServerSpecData({
                    archiveIngestorVersion: this.parentDefaultValue(), 
                    latestOTWArchiveVersion: this.parentDefaultValue()
                }));
            })
        ).subscribe((result) => {
            this.archiveServerSpecData.set(result);
        });
    }
}
