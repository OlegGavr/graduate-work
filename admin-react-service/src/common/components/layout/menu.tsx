import {Menu,useRedirect} from "react-admin";
import {MenuItem} from "@mui/material";
import ViewListIcon from "@mui/icons-material/ViewList";
import type {MenuProps as MenuType} from "react-admin";
import styles from "./styles.module.scss";

export const CustomMenu = (props: MenuType) => {
    const redirect = useRedirect();

    return (
        <Menu {...props}>
            <MenuItem className={styles["menu-item"]}
                      onClick={() => redirect("/projects")}>
                <ViewListIcon className={styles["menu-item-icon"]}/>
                Projects
            </MenuItem>
        </Menu>
    );
};
