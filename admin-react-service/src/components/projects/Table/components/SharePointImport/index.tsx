import React, {useContext, useEffect, useState} from "react";
import {Button} from "react-admin";
import Chip from "@mui/material/Chip";
import DoneIcon from "@mui/icons-material/Done";
import {SharePointLinkAvailabilityStatusDto} from "gateway-service-api-react-client";
import ErrorOutlineIcon from "@mui/icons-material/ErrorOutline";
import UpdateIcon from "@mui/icons-material/Update";
import {ProjectContext} from "../../../context/context";
import {SharePointLink} from "../SharePointLink";
import {formatDateTime} from "../../../../../common/utils/format-date-time";
import styles from "./styles.module.scss";

export function SharePointImport() {
  const {state, methods} = useContext(ProjectContext);
  const {sharePointLink, sharePointLinkStatus} = state;
  const {checkSharePointLinkStatus, updateBySharePointLink} = methods;

  const [chipProps, setChipProps] = useState<any>({});
  const [statusDate, setStatusDate] = useState<string>(new Date().toISOString());

  useEffect(() => {
    checkSharePointLinkStatus();
    const intervalId = setInterval(() => {
      checkSharePointLinkStatus();
    }, 60000);

    return () => clearInterval(intervalId);
  }, []);

  useEffect(() => {
    setStatusDate(new Date().toISOString());
    switch (sharePointLinkStatus.availabilityStatus) {
      case SharePointLinkAvailabilityStatusDto.Available:
        if (sharePointLinkStatus.needToUpdate) {
          setChipProps({
            icon: (<UpdateIcon/>),
            label: `Требуется повторная синхронизация. Дата модификации файла: ${formatDateTime(sharePointLinkStatus.sharepointUpdateTs)}, Дата модификации файла в Системе: ${formatDateTime(sharePointLinkStatus.localUpdateTs)}`,
            color: "default"
          });
        } else {
          setChipProps({
            icon: (<DoneIcon/>),
            label: "Обновление не требуется",
            color: "success"
          });
        }
        break;

      case SharePointLinkAvailabilityStatusDto.NotAvailable:
        setChipProps({
          icon: (<ErrorOutlineIcon/>),
          label: "Невозможно получить документ по ссылке",
          color: "error"
        });
        break;

      default:
        setChipProps({});
    }
  }, [sharePointLinkStatus]);

  const importFromSharePoint = () => {
    if (!sharePointLink) {
      return;
    }

    updateBySharePointLink();
  };

  return (
    <div className={styles["share-point-import"]}>
      <SharePointLink/>
      <Button
        className={!sharePointLink ? styles["import-button-disabled"] : ""}
        title={!sharePointLink ? "Укажите ссылку на файл в Sharepoint" : ""}
        label="Импорт из Sharepoint"
        onClick={importFromSharePoint}
      />

      {!!sharePointLink && (
        <div className={styles["status"]}>
          <Chip {...chipProps} variant="outlined"/>
          <span className={styles["status__date"]}>Проверка статуса: {formatDateTime(statusDate)}</span>
        </div>
      )}
    </div>
  );
}
