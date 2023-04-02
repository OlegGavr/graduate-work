import React, {useContext} from "react";
import {Loader} from "../../../../common/components/loader";
import {ProjectContext} from "../../context/context";

export function ProjectTableLoading() {
    const {state} = useContext(ProjectContext);
    const {loading} = state;

    return (loading ? <Loader/> : null);
}
