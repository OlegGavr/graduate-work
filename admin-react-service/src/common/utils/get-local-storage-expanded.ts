import type {CostProjectExpandedType} from "../types";
import {HEADERS_LOCAL_STORAGE} from "../constants/local-storage";

type LocalStorageExpandedType = {
    main: CostProjectExpandedType,
    develop: CostProjectExpandedType,
    comment: string | null,
}

export function getLocalStorageExpanded(): LocalStorageExpandedType {
    const originalExpanded = localStorage.getItem(HEADERS_LOCAL_STORAGE.ORIGINAL_STORAGE);
    const multipliedByKExpanded = localStorage.getItem(HEADERS_LOCAL_STORAGE.MULTIPLIED_BY_K_STORAGE);
    const multipliedByKWithRoundExpanded = localStorage.getItem(HEADERS_LOCAL_STORAGE.MULTIPLIED_BY_K_WITH_ROUND_STORAGE);

    const originalDevelopExpanded = localStorage.getItem(HEADERS_LOCAL_STORAGE.ORIGINAL_DEVELOP_STORAGE);
    const multipliedByKDevelopExpanded = localStorage.getItem(HEADERS_LOCAL_STORAGE.MULTIPLIED_BY_K_DEVELOP_STORAGE);
    const multipliedByKWithRoundDevelopExpanded = localStorage.getItem(HEADERS_LOCAL_STORAGE.MULTIPLIED_BY_K_WITH_ROUND_DEVELOP_STORAGE);

    const commentExpanded = localStorage.getItem(HEADERS_LOCAL_STORAGE.COMMENT_STORAGE);

    return {
        main: {
            original: originalExpanded,
            multipliedByK: multipliedByKExpanded,
            multipliedByKWithRound: multipliedByKWithRoundExpanded
        },
        develop: {
            original: originalDevelopExpanded,
            multipliedByK: multipliedByKDevelopExpanded,
            multipliedByKWithRound: multipliedByKWithRoundDevelopExpanded
        },
        comment: commentExpanded,
    };
}
