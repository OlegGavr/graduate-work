import React, {useContext} from "react";
import {Button} from "react-admin";
import type {DropdownItemType} from "../../../../../common/components/dropdown";
import {ImportEnum} from "../../../types";
import {CustomDropdown} from "../../../../../common/components/dropdown";
import {ProjectTableTemplate} from "../Template";
import {ProjectContext} from "../../../context/context";
import styles from "./styles.module.scss";
import "./style.scss";

export function ProjectTableActions() {
    const {methods} = useContext(ProjectContext);
    const {
        handleExportProject,
        loadFile,
        onChangeInput,
        onClickInput,
    } = methods;

    const importItems: DropdownItemType[] = [
        {
            value: "Оценка проекта",
            action: () => loadFile(ImportEnum.HAULMONT),
        },
        {
            value: "Скоуп работ проекта",
            action: () => loadFile(ImportEnum.HSE_PLAN),
        },
        {
            value: "Оценка и скоуп работ проекта",
            action: () => loadFile(ImportEnum.HSE_TYPE),
        },
    ];

    return (
        <div className={styles.actions}>
            <input className={styles["file-input"]}
                   onClick={onClickInput}
                   onChange={onChangeInput}
                   id="file-input"
                   type="file"/>
            <ProjectTableTemplate/>
            <Button onClick={handleExportProject}
                    label="Экспорт"/>
            <div className={styles["import-wrapper"]}>
                <Button onClick={() => loadFile(ImportEnum.AUTO)}
                        className={styles["import-button"]}
                        label="Импорт"/>
                <CustomDropdown title=""
                                className="import-dropdown"
                                items={importItems}/>
            </div>
        </div>
    );
}
