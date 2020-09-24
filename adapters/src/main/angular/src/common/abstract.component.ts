import { Control } from "./control";

export class AbstractComponent {
    closeModal(comp: any) {
        ($(comp) as any).modal('hide');
    }
    openModal(comp: any) {
        ($(comp) as any).modal({
            backdrop: 'static',
            show: true
        });
    }

    getControl(): Control {
        return (window as any).control;
    }

    exit() {
        const control = this.getControl();
        if(control) {
            control.exit();
        } else {
            console.log('control not found. running on browser?');
        }
    }
}