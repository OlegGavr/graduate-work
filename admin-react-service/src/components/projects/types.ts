// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import type {CostProjectItemDto, CostProjectRiskDto} from "gateway-service-api-react-client";
import type {PARENT_ID} from "../../common/constants/local-storage";
import type {KeyCostProjectItemType} from "../../common/types";

export enum ImportEnum {
    AUTO = "AUTO",
    HAULMONT = "HAULMONT",
    HSE_PLAN = "HSE_PLAN",
    HSE_TYPE = "HSE_TYPE",
}

export enum ContentType {
    BLOB = "BLOB",
    MEDIA = "MEDIA"
}

export type RowItem = CostProjectItemDto & {
    isExpanded?: boolean;
};

export type KeySubCostProjectItemType = PARENT_ID.DEVELOP_ORIGINAL | PARENT_ID.DEVELOP_MULTIPLIED_BY_K | PARENT_ID.DEVELOP_MULTIPLIED_BY_K_WITH_ROUND;

export type KeyCommentColumnsType = PARENT_ID.COMMENT_CONTENT;

export type KeyProjectItemType = KeyCostProjectItemType | KeySubCostProjectItemType | KeyCommentColumnsType;

export type CostProjectRiskDtoKeys = keyof CostProjectRiskDto;
