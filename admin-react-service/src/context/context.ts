import {createContext} from "react";
import type {LoadingContextProviderType} from "./types";
import {mockLoadingContextValue} from "./mock";

export const LoadingContext = createContext<LoadingContextProviderType>(mockLoadingContextValue);
