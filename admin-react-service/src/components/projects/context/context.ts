import {createContext} from "react";
import type {ProjectContextProviderType} from "./types";
import {mockProjectContextValue} from "./mock";

export const ProjectContext = createContext<ProjectContextProviderType>(mockProjectContextValue);
