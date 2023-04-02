import type {DefaultCellTypes, Row} from "@silevis/reactgrid/src/core";
import type {ChevronHeaderCell} from "./components/chevron-header";
import type {TextCommentCell} from "./components/text-comment";
import type {PARENT_ID} from "./constants/local-storage";

export type KeyCostProjectItemType = PARENT_ID.ORIGINAL | PARENT_ID.MULTIPLIED_BY_K | PARENT_ID.MULTIPLIED_BY_K_WITH_ROUND;

export type CostProjectExpandedType = {
    [K in KeyCostProjectItemType]: string | null
}

export type UpdateCells = DefaultCellTypes | ChevronHeaderCell | TextCommentCell;

export type UpdateRow = Row<UpdateCells>;

export type StylesType = Partial<CSSStyleDeclaration>;
