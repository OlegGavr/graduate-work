import React, {useContext} from "react";
import {ShowButton} from "react-admin";
import {ProjectContext} from "../../../context/context";
import {EditButtonWithConfirm} from "../EditButtonWithConfirm";

type SwitchButtonProps = {
    className?: string;
}

export const SwitchButton = (props: SwitchButtonProps) => {
    const {state} = useContext(ProjectContext);

    return (
        state.isShow ?
            <EditButtonWithConfirm className={props.className}/>
            : <ShowButton className={props.className} label="View"/>
    );
};