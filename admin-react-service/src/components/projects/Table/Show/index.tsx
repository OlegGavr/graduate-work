import React from "react";
import {ProjectContextProvider} from "../../context/provider";
import {ProjectShow} from "./ProjectShow";

export function ProjectShowWithProvider() {
    return (
        <ProjectContextProvider>
            <ProjectShow/>
        </ProjectContextProvider>
    );
}