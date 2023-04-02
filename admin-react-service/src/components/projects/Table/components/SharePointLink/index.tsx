import React, {useContext, useState, useEffect} from "react";
import EditIcon from "@mui/icons-material/Edit";
import {Form, TextInput, regex, Button} from "react-admin";
import {ShowField} from "../../../../../common/components/show-field";
import {ProjectContext} from "../../../context/context";
import styles from "./styles.module.scss";

export function SharePointLink() {
  const {state, methods} = useContext(ProjectContext);
  const {sharePointLink, isShow} = state;
  const {updateSharePointLink} = methods;

  const [editLink, setEditLink] = useState<string>("");
  const [isEdit, setIsEdit] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string>("");
  const pattern = /^(https):\/\/[^\s$.?#].[^\s]*$/;
  const labelText = "Ссылка на файл";
  const fieldWidth = 250;

  useEffect(() => {
    setEditLink(sharePointLink);
  }, [sharePointLink]);

  const saveLink = () => {
    if (!pattern.test(editLink)) {
      return;
    }

    updateSharePointLink(editLink);
    setIsEdit(false);
  };

  const changeValue = (e: any) => {
    if (!pattern.test(e.target.value)) {
      setErrorMessage("Неверный формат ссылки");
    } else {
      setErrorMessage("");
    }

    setEditLink(e.target.value);
  };

  const changeMode = () => {
    setIsEdit(!isEdit);
  };

  return (
    <Form className={styles["share-point-link"]}>
      {isEdit ? (
        <div className={styles["input-wrap"]}>
          <TextInput
            className={styles["input"]}
            style={{width: fieldWidth}}
            source="sharePointLink"
            label={labelText}
            value={editLink}
            defaultValue={editLink ?? ""}
            validate={regex(pattern)}
            onChange={changeValue}
            onBlur={saveLink}
          />

          {
            !!errorMessage && (
              <div className={styles["error"]}>
                {errorMessage}
              </div>
            )
          }
        </div>
      ) : (
        <ShowField width={fieldWidth} label={labelText}>
          <a className={styles["link"]}
             target="_blank" rel="noopener noreferrer"
             title={sharePointLink}
             href={sharePointLink}>
            {sharePointLink}
          </a>
        </ShowField>
      )}

      {
        !isShow && (
          <Button className={styles["edit-button"]}
                  label="" onClick={changeMode}>
            <EditIcon/>
          </Button>
        )
      }
    </Form>
  );
}
