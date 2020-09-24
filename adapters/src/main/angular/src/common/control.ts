export const TYPE_TEXT = 0;
export const TYPE_RADIO = 1;
export const TYPE_CHECKBOX = 2;

export interface Field {
    id: string;
    x: number;
    y: number;
    width: number;
    height: number;
    page: number;
    name: string;
    type: number;
    checkValue: string;
}


export interface Control {
    exit();

    getCommandLineArgs(): string[];

    openPdfFile(path: string): string;

    getNumberOfPages(uuid: string): number;

    transferFields(src: string, dest: string): string;

    getFields(uuid: string, page: number): Field[];

    getPageImage(uuid: string, page: number): string;

    showOpenFileDialog(): string;

    showSaveFileDialog(): string;
}