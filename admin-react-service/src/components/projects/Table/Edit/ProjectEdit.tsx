import React, {useContext} from "react";
import {Edit} from "react-admin";
import {ProjectContext} from "../../context/context";
import {TableBase} from "../components/TableBase";

export function ProjectEdit() {
    const {state} = useContext(ProjectContext);

    return (
        <Edit actions={false} title={state.name}>
            <TableBase/>
        </Edit>
    );
}