import type React from "react";
import type {LoadingBarRef} from "react-top-loading-bar";

export type LoadingContextProviderType = {
    state: {
        ref?: React.RefObject<LoadingBarRef>
    },
}
