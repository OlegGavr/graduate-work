import type {RowItem} from "../../../types";
import type {UpdateCells, UpdateRow} from "../../../../../common/types";
import {BLUE, GREEN, YELLOW} from "../../../../../common/constants/colors";
import {HEADERS_LOCAL_STORAGE, PARENT_ID} from "../../../../../common/constants/local-storage";
import {classnames} from "../../../../../common/utils/classnames";
import {CommentCell} from "../../components/CommentCell";
import {renderCells} from "./render-cells";
import styles from "./styles.module.scss";

export const getRows = (
    rows: RowItem[],
    collapsedHeaders: string[],
    isShowView: boolean,
    expandedCommentColumn: (rowId: string) => void,
): UpdateRow[] => [
    ...rows
        .map<UpdateRow>((project, index, array) => {
            const isSmall = array.length < 7;
            const isFirstAggregate = project.id!.startsWith("aggregate0");
            const isShadow = isSmall && isFirstAggregate;

            const isCommentExpanded = !JSON.parse(localStorage.getItem(HEADERS_LOCAL_STORAGE.COMMENT_STORAGE)!)
                && !!project.comment;

            const isAggregate = project.id!.startsWith("aggregate") || isShowView;
            const isHighlight = project.id!.startsWith("aggregate1") || project.id!.startsWith("aggregate2");

            const cells: UpdateCells[] = [
                {
                    type: "text",
                    groupId: "number",
                    text: String(index + 1),
                    nonEditable: true,
                    className: classnames(styles.count, {
                        "shadow-cell-bottom": isShadow
                    }),
                },
                {
                    type: "chevron",
                    groupId: "name",
                    isExpanded: project.isExpanded,
                    text: project?.name ?? "",
                    parentId: project.parentId,
                    nonEditable: isAggregate,
                    className: classnames(styles.name, {
                        "shadow-cell-bottom": isShadow
                    }),
                },
                {
                    type: "text-comment",
                    groupId: PARENT_ID.COMMENT_CONTENT,
                    text: project?.comment ?? "",
                    nonEditable: isAggregate,
                    onFocus: () => expandedCommentColumn(project.id!),
                    className: classnames(styles.comment, "comment-empty-field", {
                        "shadow-cell-bottom": isShadow,
                        "highlight-comment-cell": isCommentExpanded
                    }),
                    renderer: () => <CommentCell isCommentExpanded={isCommentExpanded}
                                                 comment={project?.comment!}/>
                },

                ...renderCells(project, `${PARENT_ID.ORIGINAL}.`, isAggregate, isShadow, isHighlight, YELLOW),
                ...renderCells(project, `${PARENT_ID.MULTIPLIED_BY_K}.`, isAggregate, isShadow, isHighlight, BLUE),
                ...renderCells(project, `${PARENT_ID.MULTIPLIED_BY_K_WITH_ROUND}.`, isAggregate, isShadow, isHighlight, GREEN),
            ];

            return {
                rowId: project.id!,
                height: 25,
                reorderable: !isAggregate,
                cells: cells.filter(cell => {
                    return !collapsedHeaders.find(header => (cell.groupId as string).includes(`${header}.`));
                }),
            };
        }),
];
