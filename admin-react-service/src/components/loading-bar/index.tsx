import React, {useContext} from "react";
import LoadingBar from "react-top-loading-bar";
import {LoadingContext} from "../../context/context";

export function LoadingTopBar() {
    const {state} = useContext(LoadingContext);

    return (
        <LoadingBar color="#ffffff" ref={state.ref}/>
    );
}
