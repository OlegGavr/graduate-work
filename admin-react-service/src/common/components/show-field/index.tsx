import React from "react";
import styles from "./styles.module.scss";

type ShowFieldProps = {
  label: string;
  children: JSX.Element;
  width?: number;
}

export function ShowField({children, width, label}: ShowFieldProps) {
  return (
    <div className={styles["show-field"]} style={{width: width ?? "auto"}}>
      <label className={styles["show-field__label"]}>{label}</label>
      <div className={styles["show-field__content-wrap"]}>
        <div className={styles["show-field__content"]}>
          {children}
        </div>
      </div>
    </div>
  );
}
