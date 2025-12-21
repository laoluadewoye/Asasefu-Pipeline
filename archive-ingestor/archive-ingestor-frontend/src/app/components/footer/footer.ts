import { Component, inject, input, InputSignal, OnInit, signal, WritableSignal } from '@angular/core';
import { ArchiveServerSpecService } from '../../services/archive-server-spec';
import { ArchiveServerSpecData } from '../../models/archive-server-spec-data';
import { catchError } from 'rxjs';

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
        this.archiveServerSpecData.set({
            archiveIngestorVersion: this.parentDefaultValue(), 
            latestOTWArchiveVersion: this.parentDefaultValue()
        });
        this.archiveServerSpecService.getArchiveServerSpecData().pipe(
            catchError((err) => {
                console.log(err);
                throw err;
            })
        ).subscribe((result) => {
            this.archiveServerSpecData.set(result);
        });
    }
}
