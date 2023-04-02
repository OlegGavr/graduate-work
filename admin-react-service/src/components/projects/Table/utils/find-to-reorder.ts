import type {DropPosition, Id} from "@silevis/reactgrid/src/core";
import type {UpdateRow} from "../../../../common/types";
import {findChevronCell} from "./find-cell";

type ToReorderType = {
    rowId: Id,
    position: DropPosition
}

export function findToReorder(
    rowsTree: UpdateRow[],
    targetRowId: Id,
    rowIds: Id[],
    dropPosition: DropPosition
): ToReorderType {
    const targetRowIndex = rowsTree.findIndex(item => item.rowId === targetRowId);
    const targetRow = rowsTree[targetRowIndex];
    const targetIndent = findChevronCell(targetRow)?.indent;

    const prevTargetRowIndex = targetRowIndex - 1;
    const prevTargetRow = targetRowIndex !== 0 ? rowsTree[prevTargetRowIndex] : null;
    const prevIndent = findChevronCell(prevTargetRow)?.indent;

    const nextTargetRowIndex = targetRowIndex + 1;
    const nextTargetRow = nextTargetRowIndex !== rowsTree.length ? rowsTree[nextTargetRowIndex] : null;
    const nextIndent = findChevronCell(nextTargetRow)?.indent;

    const reorderRow = rowsTree.find(item => item.rowId === rowIds[0]);
    const reorderIndent = findChevronCell(reorderRow)?.indent;

    if (dropPosition === "before") {
        if (reorderIndent && !targetIndent && prevIndent) {
            if (prevIndent <= reorderIndent) {
                return {
                    rowId: prevTargetRow!.rowId,
                    position: "after",
                };
            } else {
                const searchRowsTree = [...rowsTree].slice(0, prevTargetRowIndex);
                const row = searchRowsTree
                    .reverse()
                    .find(item => findChevronCell(item)?.indent === reorderIndent);

                return {
                    rowId: row!.rowId,
                    position: "after"
                };
            }

        }

        return {
            rowId: targetRowId,
            position: dropPosition,
        };
    }

    if (dropPosition === "after") {
        if (nextIndent && (!targetIndent || targetIndent && targetIndent < nextIndent)) {
            return {
                rowId: nextTargetRow?.rowId!,
                position: "before",
            };
        }

        if (!nextIndent || targetIndent && nextIndent < targetIndent) {
            const searchRowsTree = [...rowsTree].slice(0, nextTargetRowIndex);
            const row = searchRowsTree
                .reverse()
                .find(item => {
                    const itemIndent = findChevronCell(item)?.indent ?? 0;
                    const currentIndent = reorderIndent ?? 0;
                    return itemIndent <= currentIndent;
                });

            return {
                rowId: row!.rowId,
                position: "after"
            };
        }

        return {
            rowId: targetRowId,
            position: dropPosition,
        };
    }

    return {
        rowId: targetRowId,
        position: dropPosition,
    };
}
