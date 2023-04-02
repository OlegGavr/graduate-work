import React from "react";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import HighlightOffIcon from "@mui/icons-material/HighlightOff";
import {Button} from "@mui/material";
import {classnames} from "../../utils/classnames";
import "react-confirm-alert/src/react-confirm-alert.css";
import "./styles.scss";
import styles from "./styles.module.scss";

type CustomConfirmProps = {
    onClick(): void;
    onClose(): void;
    className?: string;
    header: string;
    content: React.ReactNode;
}

export function CustomConfirm(props: CustomConfirmProps) {
    const {onClick, onClose, className, header, content} = props;
    const _onClick = () => {
        onClick();
        onClose();
    };

    return (
        <div className={classnames(className, styles["custom-ui"])}>
            <h3>{header}</h3>
            <div>{content}</div>
            <div className={styles["custom-ui-buttons"]}>
                <Button className={styles["confirm-button"]} onClick={_onClick}>
                    <CheckCircleIcon/>
                    ОК
                </Button>
                <Button onClick={onClose} className={styles["cancel-button"]}>
                    <HighlightOffIcon/>
                    Отмена
                </Button>
            </div>
        </div>
    );
}