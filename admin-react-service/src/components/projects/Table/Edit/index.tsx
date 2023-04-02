import React from "react";
import {ProjectContextProvider} from "../../context/provider";
import {ProjectEdit} from "./ProjectEdit";

export function ProjectEditWithProvider() {
    return (
        <ProjectContextProvider>
            <ProjectEdit/>
        </ProjectContextProvider>
    );
}

