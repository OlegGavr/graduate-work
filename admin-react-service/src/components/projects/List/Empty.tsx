import React from "react";
import {Box, Typography} from "@mui/material";
import {Button} from "react-admin";
import {useCreateProject} from "./hook";
import "./style.scss";

export function Empty() {
    const onCreate = useCreateProject();

    return (
        <Box textAlign="center" m={1} className="box">
            <Typography variant="h4" paragraph>
                No projects available
            </Typography>
            <Button onClick={onCreate}
                    label="Create Project"/>
        </Box>
    );
}
