import React from "react";
import {useProjectContextValue} from "./value";
import {ProjectContext} from "./context";

type ProjectContextProviderProps = {
    children: React.ReactNode
};

export function ProjectContextProvider(props: ProjectContextProviderProps) {
    const value = useProjectContextValue();

    return (
        <ProjectContext.Provider value={value}>
            {props.children}
        </ProjectContext.Provider>
    );
}
