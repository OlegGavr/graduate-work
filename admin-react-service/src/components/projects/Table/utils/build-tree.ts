import type {ChevronCell} from "@silevis/reactgrid/src/core";
import type {UpdateRow} from "../../../../common/types";
import {HEADERS_LOCAL_STORAGE} from "../../../../common/constants/local-storage";
import {getMaxWidthComment} from "../../../../common/utils/get-max-width-comment";
import {getMaxHeightRow} from "../../../../common/utils/get-max-height-row";
import {findChevronCell} from "./find-cell";

const hasChildren = (rows: UpdateRow[], row: UpdateRow): boolean =>
    rows.some((r) => {
        const foundChevronCell = findChevronCell(r);
        return foundChevronCell ? foundChevronCell.parentId === row.rowId : false;
    });

const getDirectChildRows = (rows: UpdateRow[], parentRow: UpdateRow): UpdateRow[] =>
    rows.filter(
        (row) =>
            !!row.cells.find(
                (cell) =>
                    cell.type === "chevron" &&
                    (cell as ChevronCell).parentId === parentRow.rowId
            )
    );

const assignIndentAndHasChildren = (
    rows: UpdateRow[],
    parentRow: UpdateRow,
    indent = 0
) => {
    ++indent;
    getDirectChildRows(rows, parentRow).forEach((row) => {
        const foundChevronCell = findChevronCell(row);
        const hasRowChildren = hasChildren(rows, row);
        if (foundChevronCell) {
            foundChevronCell.indent = indent;
            foundChevronCell.hasChildren = hasRowChildren;
        }
        if (hasRowChildren) assignIndentAndHasChildren(rows, row, indent);
    });
};


const buildIndents = (rows: UpdateRow[]): UpdateRow[] => {
    return rows.map((row) => {
        const foundChevronCell = findChevronCell(row);
        if (foundChevronCell && !foundChevronCell.parentId) {
            const hasRowChildren = hasChildren(rows, row);
            foundChevronCell.hasChildren = hasRowChildren;
            if (hasRowChildren) assignIndentAndHasChildren(rows, row);
        }
        return row;
    });
};

export const buildTree = (rows: UpdateRow[]): UpdateRow[] => {
    const isCommentExpanded = JSON.parse(localStorage.getItem(HEADERS_LOCAL_STORAGE.COMMENT_STORAGE)!);
    const buildIndentRows = buildIndents(rows);
    const maxWidthComment = getMaxWidthComment(buildIndentRows);

    return buildIndentRows
        .map((row) => {
            const {maxHeight} = getMaxHeightRow(row, isCommentExpanded, maxWidthComment);

            return {
                ...row,
                height: maxHeight
            };
        });
};
