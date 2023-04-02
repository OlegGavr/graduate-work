import type {UpdateRow} from "../types";
import {HEADERS_LOCAL_STORAGE} from "../constants/local-storage";
import {getWidthOfText} from "./get-width-of-text";
import {findCommentCell} from "./find-comment-cell";

export function getMaxWidthComment(rows: UpdateRow[]) {
    const isCommentExpanded = JSON.parse(localStorage.getItem(HEADERS_LOCAL_STORAGE.COMMENT_STORAGE)!);

    return rows.reduce((prevValue, currentRow) => {
        const commentSize = isCommentExpanded ?
            getWidthOfText(findCommentCell(currentRow)?.text!, {}) :
            {
                height: 0,
                width: 0,
            };
        return Math.max(commentSize.width, prevValue);
    }, 0);
}
