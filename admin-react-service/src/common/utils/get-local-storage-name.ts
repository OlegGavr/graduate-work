import type {KeyProjectItemType} from "../../components/projects/types";
import {HEADERS_LOCAL_STORAGE, PARENT_ID} from "../constants/local-storage";

export function getLocalStorageName(parentId: KeyProjectItemType) {
    switch (parentId) {
        case PARENT_ID.ORIGINAL:
            return HEADERS_LOCAL_STORAGE.ORIGINAL_STORAGE;
        case PARENT_ID.MULTIPLIED_BY_K:
            return HEADERS_LOCAL_STORAGE.MULTIPLIED_BY_K_STORAGE;
        case PARENT_ID.MULTIPLIED_BY_K_WITH_ROUND:
            return HEADERS_LOCAL_STORAGE.MULTIPLIED_BY_K_WITH_ROUND_STORAGE;
        case PARENT_ID.DEVELOP_ORIGINAL:
            return HEADERS_LOCAL_STORAGE.ORIGINAL_DEVELOP_STORAGE;
        case PARENT_ID.DEVELOP_MULTIPLIED_BY_K:
            return HEADERS_LOCAL_STORAGE.MULTIPLIED_BY_K_DEVELOP_STORAGE;
        case PARENT_ID.DEVELOP_MULTIPLIED_BY_K_WITH_ROUND:
            return HEADERS_LOCAL_STORAGE.MULTIPLIED_BY_K_WITH_ROUND_DEVELOP_STORAGE;
        case PARENT_ID.COMMENT_CONTENT:
            return HEADERS_LOCAL_STORAGE.COMMENT_STORAGE;
    }
}
