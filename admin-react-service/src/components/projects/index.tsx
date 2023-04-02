import type {ResourceProps} from "react-admin";
import {ProjectList} from "./List";
import {ProjectEditWithProvider} from "./Table/Edit";
import {ProjectShowWithProvider} from "./Table/Show";

const ProjectResource: Partial<ResourceProps> = {
    list: ProjectList,
    edit: ProjectEditWithProvider,
    show: ProjectShowWithProvider,
};

export default ProjectResource;
