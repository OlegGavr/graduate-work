import type {Dispatch, SetStateAction} from "react";
import type {Id} from "@silevis/reactgrid/src/core";
import type {RowItem} from "../../types";

// eslint-disable-next-line @typescript-eslint/ban-types
const reorderArray = <T extends {}>(arr: T[], idxs: number[], to: number) => {
    const movedElements = arr.filter((_, idx) => idxs.includes(idx));
    const targetIdx = Math.min(...idxs) < to ? to += 1 : to -= idxs.filter(idx => idx < to).length;
    const leftSide = arr.filter((_, idx) => idx < targetIdx && !idxs.includes(idx));
    const rightSide = arr.filter((_, idx) => idx >= targetIdx && !idxs.includes(idx));
    return [...leftSide, ...movedElements, ...rightSide];
};

export function rowsReorder(
    setProjects: Dispatch<SetStateAction<RowItem[]>>,
    targetRowId: Id,
    rowIds: Id[]
) {
    setProjects((prevProjects: RowItem[]) => {
        const to = prevProjects.findIndex(project => project.id === targetRowId);

        const rowsIndex = prevProjects.reduce((prevRowsIndex: number[], currentRow, index) => {
            if (currentRow.id === rowIds[0] || currentRow.parentId === rowIds[0]) {
                return [...prevRowsIndex, index];
            }
            return prevRowsIndex;
        }, []);

        return to > -1 ? reorderArray(prevProjects, rowsIndex, to) : prevProjects;
    });
}
