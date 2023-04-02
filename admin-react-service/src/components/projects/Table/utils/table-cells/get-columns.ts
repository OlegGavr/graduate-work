import type {Column} from "@silevis/reactgrid/src/core";
import {columnsWidth, nameWidth} from "../../../../../common/constants/table";
import {HEADERS_LOCAL_STORAGE, PARENT_ID} from "../../../../../common/constants/local-storage";
import {getWidthColumnComment} from "../../../../../common/utils/get-width-column-comment";

const baseColumnOptions = {
    reorderable: true,
    resizable: false,
    width: columnsWidth,
};

const sumColumnOptions = {
    reorderable: true,
    resizable: false,
    width: 160,
};

function renderColumns(id: string): Column[] {
    return [
        {columnId: `develop.${id}.backendCost`, ...baseColumnOptions},
        {columnId: `develop.${id}.frontendCost`, ...baseColumnOptions},
        {columnId: `${id}.devCost`, ...baseColumnOptions},
        {columnId: `${id}.qaCost`, ...baseColumnOptions},
        {columnId: `${id}.analyseCost`, ...baseColumnOptions},
        {columnId: `${id}.devOpsCost`, ...baseColumnOptions},
        {columnId: `${id}.tmCost`, ...baseColumnOptions},
        {columnId: `${id}.pmCost`, ...baseColumnOptions},
        {columnId: `${id}.sumCost`, ...sumColumnOptions},
    ];
}

export const getColumns = (
    collapsedHeaders: string[],
    maxWidthComment: number,
    ): Column[] => {
    const isCommentExpanded = JSON.parse(localStorage.getItem(HEADERS_LOCAL_STORAGE.COMMENT_STORAGE)!);
    const columns = [
        {columnId: "number", reorderable: true, resizable: false, width: 1},
        {columnId: "name", reorderable: true, resizable: false, width: nameWidth},
        {
            columnId: "comment", reorderable: true, resizable: false,
            width: getWidthColumnComment(isCommentExpanded, maxWidthComment),
        },

        ...renderColumns(PARENT_ID.ORIGINAL),
        ...renderColumns(PARENT_ID.MULTIPLIED_BY_K),
        ...renderColumns(PARENT_ID.MULTIPLIED_BY_K_WITH_ROUND),

    ];

    return columns.filter(column => {
        return !collapsedHeaders.find(header =>
            (column.columnId as string).includes(`${header}.`) &&
            !(column.columnId as string).endsWith("sumCost")
        );
    });
};
