import React, {useContext} from "react";
import {Button, useDeleteMany, useListContext} from "react-admin";
import DeleteForeverIcon from "@mui/icons-material/DeleteForever";
import {confirmAlert} from "react-confirm-alert";
import {CustomConfirm} from "../confirm";
import {LoadingContext} from "../../../context/context";
import styles from "./styles.module.scss";

type CustomDeleteButtonConfirmProps = {
    confirmTitle: string;
    confirmContent: string;
}

export function CustomDeleteButtonConfirm(props: CustomDeleteButtonConfirmProps) {
    const {state} = useContext(LoadingContext);
    const {confirmContent, confirmTitle = ""} = props;
    const {selectedIds, onUnselectItems} = useListContext();

    const [deleteMany] = useDeleteMany(
        "projects",
        {ids: selectedIds}
    );

    const handleConfirm = () => {
        state.ref?.current?.continuousStart();
        deleteMany()
            .finally(() => {
                onUnselectItems();
                state.ref?.current?.complete();
            });
    };

    const onClick = () => {
        confirmAlert({
            // eslint-disable-next-line react/no-unstable-nested-components
            customUI: ({onClose}: any) => {
                return (
                    <CustomConfirm header={confirmTitle}
                                   content={confirmContent}
                                   onClick={() => {
                                        handleConfirm();
                                    }}
                                   onClose={onClose}
                    />
                );
            }
        });
    };

    return (
        <>
            <Button startIcon={<DeleteForeverIcon/>}
                    label="Удалить"
                    className={styles["delete-button"]}
                    onClick={onClick}
            />
        </>
    );
}
