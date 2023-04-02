import type {ChevronHeaderCell} from "../../../../../common/components/chevron-header";
import type {KeyProjectItemType, KeySubCostProjectItemType} from "../../../types";
import type {KeyCostProjectItemType, UpdateRow} from "../../../../../common/types";
import {getLocalStorageName} from "../../../../../common/utils/get-local-storage-name";
import {HEADERS_LOCAL_STORAGE, PARENT_ID} from "../../../../../common/constants/local-storage";
import {getLocalStorageExpanded} from "../../../../../common/utils/get-local-storage-expanded";

const extraSubHeaderStyle = {
    style: {
        background: "#e6f3fe"
    }
};

const subHeaderStyle = {
    style: {
        background: "#b4dafd"
    }
};

const headerStyle = {
    style: {
        background: "#83c1fc"
    }
};

function getExpanded(expanded: string | null, initialExpanded = false) {
    return expanded === null ? initialExpanded : JSON.parse(expanded);
}

function getText(parentId: KeyCostProjectItemType) {
    switch (parentId) {
        case PARENT_ID.ORIGINAL:
            return "Первичная оценка";
        case PARENT_ID.MULTIPLIED_BY_K:
            return "Оценка с риском";
        case PARENT_ID.MULTIPLIED_BY_K_WITH_ROUND:
            return "Оценка округленная";
    }
}

export function getChildrenHeader(parentId: KeyProjectItemType) {
    const isSubHeader = parentId.includes("develop");
    const commentHeaders = parentId === PARENT_ID.COMMENT_CONTENT;

    if (commentHeaders) {
        return [];
    }

    return isSubHeader ?
        getSubHeaderWithParent(parentId as KeySubCostProjectItemType) :
        getHeaderWithParent(parentId as KeyCostProjectItemType);
}

function getSubHeaderWithParent(parentId: KeySubCostProjectItemType): ChevronHeaderCell[] {
    return [
        {type: "chevron-header", parentId, text: "BD", ...extraSubHeaderStyle},
        {type: "chevron-header", parentId, text: "FD", ...extraSubHeaderStyle},
    ];
}

function getHeaderWithParent(
    parentId: KeyCostProjectItemType,
    initialExpanded = false
): ChevronHeaderCell[] {
    const {develop} = getLocalStorageExpanded();
    const isExpanded = getExpanded(develop[parentId], initialExpanded);
    const chevronId = `develop.${parentId}` as KeySubCostProjectItemType;
    localStorage.setItem(getLocalStorageName(chevronId), String(isExpanded));

    const childHeaders = isExpanded ? getSubHeaderWithParent(chevronId) : [];

    return [
        ...childHeaders,
        {type: "chevron-header", parentId, chevronId, text: "Dev", hasChildren: true, isExpanded, ...subHeaderStyle},
        {type: "chevron-header", parentId, text: "QA", ...subHeaderStyle},
        {type: "chevron-header", parentId, text: "BA", ...subHeaderStyle},
        {type: "chevron-header", parentId, text: "DOps", ...subHeaderStyle},
        {type: "chevron-header", parentId, text: "TM", ...subHeaderStyle},
        {type: "chevron-header", parentId, text: "PM", ...subHeaderStyle},
    ];
}

function getHeaders(
    parentId: KeyCostProjectItemType,
    expanded: string | null,
    initialExpanded = false
): ChevronHeaderCell[] {
    const isExpanded = getExpanded(expanded, initialExpanded);
    const childHeaders = isExpanded ? getHeaderWithParent(parentId) : [];
    localStorage.setItem(getLocalStorageName(parentId), String(isExpanded));

    return [
        ...childHeaders,
        {
            type: "chevron-header",
            text: getText(parentId),
            chevronId: parentId,
            hasChildren: true,
            isExpanded,
            ...headerStyle
        },
    ];
}

export const headerRow = (): UpdateRow => {
    const {main, comment} = getLocalStorageExpanded();
    const commentExpanded = comment === null ? true : JSON.parse(comment);
    localStorage.setItem(HEADERS_LOCAL_STORAGE.COMMENT_STORAGE, String(commentExpanded));

    return {
        rowId: "headerRow",
        reorderable: false,
        cells: [
            {type: "header", text: "№"},
            {type: "header", text: "Project Name/Cost"},

            {
                id: "chevron-comment",
                type: "chevron-header",
                chevronId: PARENT_ID.COMMENT_CONTENT,
                text: "Комментарий",
                isSingle: true,
                hasChildren: true,
                isExpanded: commentExpanded,
                ...headerStyle,
            },

            ...getHeaders(PARENT_ID.ORIGINAL, main.original, true),
            ...getHeaders(PARENT_ID.MULTIPLIED_BY_K, main.multipliedByK),
            ...getHeaders(PARENT_ID.MULTIPLIED_BY_K_WITH_ROUND, main.multipliedByKWithRound),
        ]
    };
};
