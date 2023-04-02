import type {UpdateRow} from "../types";
import type {TextCommentCell} from "../components/text-comment";
import {PARENT_ID} from "../constants/local-storage";

export const findCommentCell = (row?: UpdateRow | null) =>
    row ? row.cells.find((cell) => cell.groupId === PARENT_ID.COMMENT_CONTENT) as TextCommentCell | undefined : undefined;
