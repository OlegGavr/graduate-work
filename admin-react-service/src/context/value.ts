import {useRef} from "react";
import type {LoadingBarRef} from "react-top-loading-bar";
import type {LoadingContextProviderType} from "./types";

export function useLoadingContextValue(): LoadingContextProviderType {
    const ref = useRef<LoadingBarRef>(null);

    return {
        state: {
            ref
        }
    };
}
