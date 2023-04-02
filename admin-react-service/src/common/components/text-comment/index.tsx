import * as React from "react";
import {
    getCharFromKeyCode, isAlphaNumericKey, isNavigationKey,
    getCellProperty, keyCodes
} from "@silevis/reactgrid/src/core";
import type {Cell, CellTemplate,
    Compatible, Uncertain, UncertainCompatible
} from "@silevis/reactgrid/src/core";

export interface TextCommentCell extends Cell {
    type: "text-comment",
    text: string,
    placeholder?: string;
    validator?: (text: string) => boolean,
    renderer?: (text: string) => React.ReactNode,
    onFocus?: () => void,
    errorMessage?: string,
    className?: string
}

export class TextCommentCellTemplate implements CellTemplate<TextCommentCell> {
    getCompatibleCell(uncertainCell: Uncertain<TextCommentCell>): Compatible<TextCommentCell> {
        const text = getCellProperty(uncertainCell, "text", "string");
        let placeholder: string | undefined;
        try {
            placeholder = getCellProperty(uncertainCell, "placeholder", "string");
        } catch {
            placeholder = "";
        }
        const value = parseFloat(text);
        return {...uncertainCell, text, value, placeholder};
    }

    update(cell: Compatible<TextCommentCell>, cellToMerge: UncertainCompatible<TextCommentCell>): Compatible<TextCommentCell> {
        return this.getCompatibleCell({...cell, text: cellToMerge.text, placeholder: cellToMerge.placeholder});
    }

    handleKeyDown(cell: Compatible<TextCommentCell>, keyCode: number, ctrl: boolean, shift: boolean, alt: boolean): { cell: Compatible<TextCommentCell>, enableEditMode: boolean } {
        const char = getCharFromKeyCode(keyCode, shift);
        if (!ctrl && !alt && isAlphaNumericKey(keyCode) && !(shift && keyCode === keyCodes.SPACE))
            return {cell: this.getCompatibleCell({...cell, text: shift ? char : char.toLowerCase()}), enableEditMode: true};
        return {cell, enableEditMode: keyCode === keyCodes.POINTER || keyCode === keyCodes.ENTER};
    }

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    getClassName(cell: Compatible<TextCommentCell>, isInEditMode: boolean): string {
        const isValid = cell.validator ? cell.validator(cell.text) : true;
        const className = cell.className ? cell.className : "";
        return `${isValid ? "valid" : "invalid"} ${cell.placeholder && cell.text === "" ? "placeholder" : ""} ${className}`;
    }

    render(cell: Compatible<TextCommentCell>, isInEditMode: boolean, onCellChanged: (cell: Compatible<TextCommentCell>, commit: boolean) => void): React.ReactNode {

        if (!isInEditMode) {
            const isValid = cell.validator ? cell.validator(cell.text) : true;
            const cellText = cell.text || cell.placeholder || "";
            const textToDisplay = !isValid && cell.errorMessage ? cell.errorMessage : cellText;
            return cell.renderer ? cell.renderer(textToDisplay) : textToDisplay;
        }

        return <input
            ref={input => {
                if (input) {
                    input.focus();
                    input.setSelectionRange(input.value.length, input.value.length);
                }
            }}
            defaultValue={cell.text}
            onChange={e => onCellChanged(this.getCompatibleCell({...cell, text: e.currentTarget.value}), false)}
            onFocus={cell.onFocus}
            onBlur={e => onCellChanged(this.getCompatibleCell({...cell, text: e.currentTarget.value}), (e as any).view?.event?.keyCode !== keyCodes.ESCAPE)}
            onCopy={e => e.stopPropagation()}
            onCut={e => e.stopPropagation()}
            onPaste={e => e.stopPropagation()}
            onPointerDown={e => e.stopPropagation()}
            placeholder={cell.placeholder}
            onKeyDown={e => {
                if (isAlphaNumericKey(e.keyCode) || (isNavigationKey(e.keyCode))) e.stopPropagation();
            }}
        />;
    }
}
