import { Component, input, InputSignal, output, OutputEmitterRef } from '@angular/core';
import { stringify as yamlStringify } from 'yaml';
import { XMLBuilder } from 'fast-xml-parser'

@Component({
  selector: 'app-output-icons',
  imports: [],
  templateUrl: './output-icons.html',
  styleUrl: './output-icons.css',
})
export class OutputIcons {
    dropdownEvent: OutputEmitterRef<void> = output<void>();
    inputObject: InputSignal<object | undefined> = input.required<object | undefined>();
    objectName: InputSignal<string | undefined> = input.required<string | undefined>();
    showDropdown: InputSignal<boolean> = input.required<boolean>();

    dropdownClicked() {
        this.dropdownEvent.emit();
    }

    saveBlob(blob: Blob, ext: string) {
        const objectURL = window.URL.createObjectURL(blob);
        const tempAnchor = document.createElement('a');
        tempAnchor.href = objectURL;
        tempAnchor.download = `${this.objectName()}.${ext}`;
        document.body.appendChild(tempAnchor);
        tempAnchor.click();
        document.body.removeChild(tempAnchor);
    }

    downloadJSON() {
        let jsonBlob = new Blob(
            [JSON.stringify(this.inputObject(), null, 4)], {type: "application/json"}
        );
        this.saveBlob(jsonBlob, "json");
    }

    downloadYAML() {
        let yamlBlob = new Blob(
            [yamlStringify(this.inputObject())], {type: "application/yaml"}
        );
        this.saveBlob(yamlBlob, "yaml");
    }

    downloadXML() {
        let xmlBlob = new Blob(
            [new XMLBuilder({format: true}).build(this.inputObject())], {type: "application/xml"}
        );
        this.saveBlob(xmlBlob, "xml");
    }
}
