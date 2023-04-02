import type {UpdateRow} from "../types";
import {getLocalStorageExpanded} from "./get-local-storage-expanded";
import {getMaxHeightRow} from "./get-max-height-row";
import {getMaxWidthComment} from "./get-max-width-comment";

export function handleCommentInputFocus(rows: UpdateRow[], row: UpdateRow) {
    const {comment} = getLocalStorageExpanded();
    const commentExpanded = comment === null ? true : JSON.parse(comment);

    if (commentExpanded) {
        return;
    }

    const chevronComment = document.getElementById("chevron-comment");
    chevronComment!.click();

    const inputFiled = document.body.querySelector(".rg-text-comment-celleditor") as HTMLElement;
    const maxWidthComment = getMaxWidthComment(rows);
    const {widthColumnComment, maxHeight} = getMaxHeightRow(row!, !commentExpanded, maxWidthComment);

    inputFiled!.style.width = `${widthColumnComment}px`;
    inputFiled!.style.height = `${maxHeight}px`;
}
