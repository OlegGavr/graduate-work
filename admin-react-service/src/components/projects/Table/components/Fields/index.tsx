import React, {useContext, useEffect, useState} from "react";
import {Form, useGetOne} from "react-admin";
import {useParams} from "react-router-dom";
import {CustomInput} from "../../../../../common/components/input";
import {ProjectTableActions} from "../Actions";
import {ProjectContext} from "../../../context/context";
import {CustomNameInput} from "../../../../../common/components/input/name";
import styles from "./styles.module.scss";
import "./style.scss";

export function ProjectTableFields() {
    const {state, methods} = useContext(ProjectContext);
    const {name, moneyPerHour, risk, isShow} = state;
    const {
        updateName,
        onChangeMoneyPerHour,
        onChangeRisks,
    } = methods;

    const {id} = useParams();
    const {data} = useGetOne("projects", {id});
    const placeholder = `Use default (${risk._default || 0})`;

    const [editName, setEditName] = useState<string>("");
    const [projectName, setProjectName] = useState<string>("");

    useEffect(() => {
        setEditName(name);
    }, [name]);

    useEffect(() => {
        setProjectName(data?.name);
    }, [data]);

    useEffect(() => {
        if (editName !== projectName) {
            return;
        }

    }, [editName, projectName]);

    const onSave = (value: string) => {
        if (value.length > 40) {
            return;
        }

        setEditName(value);
        updateName(value);
        setProjectName(value);
    };

    return (
        <div title="Project" className="form">
            <Form className={styles["form-container"]}>
                <div className={styles["project-info-block"]}>
                    <div className={styles["name-wrapper"]}>
                        <CustomNameInput name="name"
                                         maxLength={40}
                                         value={editName}
                                         className="name"
                                         label="Name"
                                         onChange={onSave}/>
                    </div>
                    <ProjectTableActions/>
                </div>
                <div className={styles["risks-wrapper"]}>
                    <div className={styles["general-info-wrapper"]}>
                        <div className={styles["points-title"]}>
                            Общие пункты:
                        </div>
                        <div className={styles["general-info"]}>
                            <CustomInput label="moneyPerHour"
                                         disabled={isShow}
                                         name="moneyPerHour"
                                         className={`${styles["main-field"]} money`}
                                         value={String(moneyPerHour)}
                                         onChange={onChangeMoneyPerHour}/>
                            <CustomInput label="Default factor"
                                         disabled={isShow}
                                         name="_default"
                                         value={risk._default}
                                         className={styles["main-field"]}
                                         onChange={onChangeRisks}/>
                        </div>
                    </div>
                    <div>
                        <div className={styles["points-title"]}>
                            Прочие коэффициенты:
                        </div>
                        <div className={styles["general-info"]}>
                            <CustomInput label="Front factor"
                                         disabled={isShow}
                                         name="dev"
                                         value={risk.dev}
                                         className={styles["risk-field"]}
                                         placeholder={placeholder}
                                         onChange={onChangeRisks}/>
                            <CustomInput label="QA factor"
                                         disabled={isShow}
                                         name="qa"
                                         value={risk.qa}
                                         className={styles["risk-field"]}
                                         placeholder={placeholder}
                                         onChange={onChangeRisks}/>
                            <CustomInput label="BA factor"
                                         disabled={isShow}
                                         name="ba"
                                         value={risk.ba}
                                         className={styles["risk-field"]}
                                         placeholder={placeholder}
                                         onChange={onChangeRisks}/>
                            <CustomInput label="DevOps factor"
                                         disabled={isShow}
                                         name="devOps"
                                         value={risk.devOps}
                                         className={styles["risk-field"]}
                                         placeholder={placeholder}
                                         onChange={onChangeRisks}/>
                            <CustomInput label="TM factor"
                                         disabled={isShow}
                                         name="tm"
                                         value={risk.tm}
                                         className={styles["risk-field"]}
                                         placeholder={placeholder}
                                         onChange={onChangeRisks}/>
                            <CustomInput label="PM factor"
                                         disabled={isShow}
                                         name="pm"
                                         value={risk.pm}
                                         className={styles["risk-field"]}
                                         placeholder={placeholder}
                                         onChange={onChangeRisks}/>
                        </div>
                    </div>
                </div>
            </Form>
        </div>
    );
}
