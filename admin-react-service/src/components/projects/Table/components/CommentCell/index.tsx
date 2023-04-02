import React from "react";
import {Tooltip} from "@chakra-ui/react";
import styles from "./styles.module.scss";

type CommentCellProps = {
    isCommentExpanded: boolean,
    comment: string,
}

export function CommentCell(props: CommentCellProps) {
    const {isCommentExpanded, comment} = props;

    return isCommentExpanded ? (
        <Tooltip label={comment} fontSize='md'>
            <span className={styles["comment-tooltip"]}>
                {comment}
            </span>
        </Tooltip>
    ) : (
        <span>
            {comment}
        </span>
    );
}
