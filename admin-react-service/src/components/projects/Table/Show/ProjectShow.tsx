import React, {useContext} from "react";
import {Show} from "react-admin";
import {ProjectContext} from "../../context/context";
import {TableBase} from "../components/TableBase";

export function ProjectShow() {
    const {state} = useContext(ProjectContext);

    return (
        <Show actions={false} title={state.name}>
            <TableBase/>
        </Show>
    );
}