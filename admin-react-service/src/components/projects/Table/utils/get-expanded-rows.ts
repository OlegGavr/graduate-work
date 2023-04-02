import type {UpdateRow} from "../../../../common/types";
import {findChevronCell} from "./find-cell";

const findParentRow = (rows: UpdateRow[], row: UpdateRow) =>
    rows.find((r) => {
        const foundChevronCell = findChevronCell(row);
        return foundChevronCell ? r.rowId === foundChevronCell.parentId : false;
    });

const isRowFullyExpanded = (rows: UpdateRow[], row: UpdateRow): boolean => {
    const parentRow = findParentRow(rows, row);
    if (parentRow) {
        const foundChevronCell = findChevronCell(parentRow);
        if (foundChevronCell && !foundChevronCell.isExpanded) return false;
        return isRowFullyExpanded(rows, parentRow);
    }
    return true;
};

export const getExpandedRows = (rows: UpdateRow[]): UpdateRow[] => {
    return rows.filter((row) => {
        const areAllParentsExpanded = isRowFullyExpanded(rows, row);
        return areAllParentsExpanded !== undefined ? areAllParentsExpanded : true;
    });
};