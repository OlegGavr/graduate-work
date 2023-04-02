import React from "react";
import {ChakraProvider} from "@chakra-ui/react";
import {ProjectTableLoading} from "../Loading";
import {ProjectTableFields} from "../Fields";
import {SwitchButton} from "../SwitchButton";
import {ProjectTableGrid} from "../Grid";
import {SharePointImport} from "../SharePointImport";
import {HEIGHT_TABLE_ROW} from "../../../../../common/constants/table";
import styles from "./styles.module.scss";

export function TableBase() {
    return (
        <ChakraProvider>
            <ProjectTableLoading/>
            <ProjectTableFields/>
            <span className={styles["width-name"]}
                  style={{lineHeight: `${HEIGHT_TABLE_ROW}px`}}
                  id="width-name"/>
            <div className={styles["toolbar"]}>
                <SharePointImport/>
                <SwitchButton className={styles["switch-button"]}/>
            </div>
            <ProjectTableGrid/>
        </ChakraProvider>
    );
}
