import {closeCommentWidth, maxOpenCommentWidth, smallOpenCommentWidth} from "../constants/table";

const VERTICAL_MARGINS = 8;

export function getWidthColumnComment(isCommentExpanded: boolean, maxWidthComment: number) {
    return isCommentExpanded ? (
        maxWidthComment > maxOpenCommentWidth ? maxOpenCommentWidth :
            maxWidthComment < smallOpenCommentWidth - VERTICAL_MARGINS ? smallOpenCommentWidth : maxWidthComment
    ) : closeCommentWidth;
}
