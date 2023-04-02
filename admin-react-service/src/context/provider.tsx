import React from "react";
import {useLoadingContextValue} from "./value";
import {LoadingContext} from "./context";

type LoadingContextProviderProps = {
    children: React.ReactNode
};

export function LoadingContextProvider(props: LoadingContextProviderProps) {
    const value = useLoadingContextValue();

    return (
        <LoadingContext.Provider value={value}>
            {props.children}
        </LoadingContext.Provider>
    );
}
