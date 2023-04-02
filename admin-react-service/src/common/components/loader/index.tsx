import React from "react";
import styles from "./styles.module.scss";

export function Loader() {
    return (
        <div className={styles["table-container"]}>
            <div className={styles["lds-ring"]}>
                <div/>
                <div/>
                <div/>
                <div/>
            </div>
        </div>
    );
}
