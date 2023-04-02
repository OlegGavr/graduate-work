import {
    getCellProperty,
    getCharFromKeyCode,
    isAlphaNumericKey,
    keyCodes
} from "@silevis/reactgrid/src/core";
import * as React from "react";
import type {
    Cell,
    CellStyle,
    CellTemplate,
    Compatible,
    Id,
    Span,
    Uncertain,
    UncertainCompatible
} from "@silevis/reactgrid/src/core";
import type {KeyProjectItemType} from "../../../components/projects/types";
import {getLocalStorageName} from "../../utils/get-local-storage-name";
import {classnames} from "../../utils/classnames";
import styles from "./styles.module.scss";
import "./style.scss";

export interface ChevronHeaderCell extends Cell, Span {
    type: "chevron-header";
    text: string;
    id?: string;
    isSingle?: boolean;
    isExpanded?: boolean;
    hasChildren?: boolean;
    chevronId?: Id;
    parentId?: Id;
}

export class ChevronHeaderCellTemplate implements CellTemplate<ChevronHeaderCell> {
    getCompatibleCell(uncertainCell: Uncertain<ChevronHeaderCell>): Compatible<ChevronHeaderCell> {
        const text = getCellProperty(uncertainCell, "text", "string");

        let isExpanded: boolean;
        try {
            isExpanded = getCellProperty(uncertainCell, "isExpanded", "boolean");
        } catch {
            isExpanded = true;
        }

        let isSingle: boolean;
        try {
            isSingle = getCellProperty(uncertainCell, "isSingle", "boolean");
        } catch {
            isSingle = false;
        }

        let hasChildren: boolean;
        try {
            hasChildren = getCellProperty(uncertainCell, "hasChildren", "boolean");
        } catch {
            hasChildren = false;
        }

        const value = parseFloat(text);
        return {...uncertainCell, text, value, isExpanded, isSingle, hasChildren};
    }

    handleKeyDown(cell: Compatible<ChevronHeaderCell>, keyCode: number, ctrl: boolean, shift: boolean, alt: boolean): { cell: Compatible<ChevronHeaderCell>, enableEditMode: boolean } {
        let enableEditMode = keyCode === keyCodes.POINTER || keyCode === keyCodes.ENTER;
        const cellCopy = {...cell};
        const char = getCharFromKeyCode(keyCode, shift);
        if (keyCode === keyCodes.SPACE && cellCopy.isExpanded !== undefined && !shift) {
            cellCopy.isExpanded = !cellCopy.isExpanded;
        } else if (!ctrl && !alt && isAlphaNumericKey(keyCode) && !(shift && keyCode === keyCodes.SPACE)) {
            cellCopy.text = !shift ? char.toLowerCase() : char;
            enableEditMode = true;
        }
        return {cell: cellCopy, enableEditMode};
    }

    update(cell: Compatible<ChevronHeaderCell>, cellToMerge: UncertainCompatible<ChevronHeaderCell>): Compatible<ChevronHeaderCell> {
        localStorage.setItem(getLocalStorageName(cell.chevronId as KeyProjectItemType), String(cellToMerge.isExpanded));
        return this.getCompatibleCell({...cell, isExpanded: cellToMerge.isExpanded});
    }

    isFocusable = () => false;

    getClassName(cell: Compatible<ChevronHeaderCell>): string {
        return classnames(cell.className, {
            "expanded": cell.hasChildren! && cell.isExpanded!,
            "collapsed": cell.hasChildren! && !cell.isExpanded,
            "single-expanded": cell.isSingle!,
        });
    }

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    getStyle(cell: Compatible<ChevronHeaderCell>, isInEditMode: boolean): CellStyle {
        return {background: "rgba(128, 128, 128, 0.1)"};
    }

    render(cell: Compatible<ChevronHeaderCell>, isInEditMode: boolean, onCellChanged: (cell: Compatible<ChevronHeaderCell>, commit: boolean) => void): React.ReactNode {
        const text = cell.isSingle && !cell.isExpanded ? `${cell.text.substr(0, 4)}.` : cell.text;
        return (
            <>
                {cell.hasChildren ?
                    <div id={cell.id}
                         className={`chevron-icon ${styles["chevron-header"]}`}
                         onClick={e => {
                             e.stopPropagation();
                             onCellChanged(this.getCompatibleCell({...cell, isExpanded: !cell.isExpanded}), true);
                         }}
                    >
                        <span className={styles.icon}>❮</span>
                    </div>
                    :
                    <div className='no-child'/>
                }
                <div className="chevron-header-text">
                    {text}
                </div>
            </>
        );
    }
}
