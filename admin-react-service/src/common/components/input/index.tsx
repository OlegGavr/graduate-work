import React, {useState} from "react";
import type {CustomInputProps} from "./types";
import {classnames} from "../../utils/classnames";
import styles from "./styles.module.scss";

export function CustomInput(props: CustomInputProps) {
    const {
        value, label, className, name,
        placeholder, onChange, disabled
    } = props;
    const [isFocus, setIsFocus] = useState<boolean>(false);

    return (
        <div className={`${styles.root} ${className}`}>
            <label className={classnames(styles["label-container"], {
                        [styles["focus-label"]]: isFocus,
                        [styles["filled-label"]]: value !== undefined,
                    })}>
                <span>
                    {label}
                </span>
            </label>
            <div className={classnames(styles["input-container"], {
                [styles["focus-input-container"]]: isFocus
            })}>
                <input className={styles.input}
                       disabled={disabled}
                       placeholder={placeholder}
                       value={value ?? ""}
                       name={name}
                       onFocus={() => setIsFocus(true)}
                       onBlur={() => setIsFocus(false)}
                       onChange={onChange}/>
            </div>
        </div>
    );
}
