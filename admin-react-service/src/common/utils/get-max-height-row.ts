import type {UpdateRow,StylesType} from "../types";
import {findChevronCell} from "../../components/projects/Table/utils";
import {getWidthOfText} from "./get-width-of-text";
// import {getWidthColumnComment} from "./get-width-column-comment";
import {findCommentCell} from "./find-comment-cell";

// const WIDTH_INDENT = 22.4;
// const WIDTH_CLASSIC_PADDING = 45;
// const WIDTH_NAME_PADDING = 70;

export function getMaxHeightRow(
    row: UpdateRow,
    isCommentExpanded: boolean,
    maxWidthComment: number
) {
    const indent = findChevronCell(row)?.indent ?? 0;
    // const widthColumnComment = getWidthColumnComment(isCommentExpanded, maxWidthComment);

    const nameStyles: StylesType = {
        paddingLeft: `${22.4 * indent + 6 + 16}px`,
        width: "300px",
    };

    const commentStyles: StylesType = {
        paddingLeft: "6px",
        maxWidth: `${maxWidthComment}px`,
    };

    const textSize = getWidthOfText(findChevronCell(row)!.text, nameStyles);
    const commentSize = isCommentExpanded ?
        getWidthOfText(findCommentCell(row)!.text, commentStyles) :
        {
            width: 0,
            height: 0
        };

    const maxHeight = Math.max(textSize.height, commentSize.height);

    return {
        widthColumnComment: commentSize.width,
        maxHeight
    };
}
