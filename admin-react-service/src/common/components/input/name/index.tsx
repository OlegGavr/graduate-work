import React, {useEffect, useRef, useState} from "react";
import EditIcon from "@mui/icons-material/Edit";
import {Input, InputGroup, InputRightElement} from "@chakra-ui/react";
import {Button} from "react-admin";
import type {CustomInputProps} from "../types";
import type {StylesType} from "../../../types";
import {getWidthOfText} from "../../../utils/get-width-of-text";
import styles from "./styles.module.scss";

type CustomNameInputProps = Omit<CustomInputProps, "onChange"> & {
    onChange(name: string): void;
}

const FONT_SIZE = 1.3;
const WIDTH_RIGHT_ELEMENT = 40;

const NAME_STYLES: StylesType = {
    whiteSpace: "nowrap",
    maxWidth: "500px"
};

export function CustomNameInput(props: CustomNameInputProps) {
    const {value, maxLength, onChange} = props;
    const ref = useRef<HTMLInputElement>(null);
    const [name, setName] = useState<string>("");
    const [isFocus, setIsFocus] = useState<boolean>(false);
    const [widthInput, setWidthInput] = useState<number>(100);

    useEffect(() => {
        if (value) {
            console.log(value);
            setWidthInput(getWidthOfText(value as string, NAME_STYLES).width);
            setName(value as string);
        }
    }, [value]);

    const onChangeName = (e: React.ChangeEvent<HTMLInputElement>) => {
        const valueName = e.target.value.slice(0, maxLength);

        if (valueName.length !== 0) {
            console.log(valueName);
            setWidthInput(getWidthOfText(valueName, NAME_STYLES).width);
        }
        setName(valueName);
    };

    const onClickIcon = () => {
        Promise.resolve(setIsFocus(true))
            .then(() => {
                ref.current!.focus();
            });
    };

    const onBlurInput = () => {
        if (name.length !== 0 && name !== value) {
            onChange(name);
        } else {
            setWidthInput(getWidthOfText(value as string, NAME_STYLES).width);
            setName(value as string);
        }

        setIsFocus(false);
    };

    return (
        <InputGroup className={styles["name-input-group"]}
        >
            <Input
                ref={ref}
                style={{width: `${FONT_SIZE * widthInput + WIDTH_RIGHT_ELEMENT}px`}}
                disabled={!isFocus}
                onBlur={onBlurInput}
                className={styles["name-input"]}
                value={name}
                onChange={onChangeName}
                variant="flushed"
                type="text"/>
                {
                    !isFocus ? (
                        <InputRightElement>
                            <Button label=""
                                    onClick={onClickIcon}
                                    className={styles["edit-button"]}>
                                <EditIcon/>
                            </Button>
                        </InputRightElement>
                    ) : (
                        <div className={styles["count-info"]}>
                            {name.length}/{maxLength}
                        </div>
                    )
                }
        </InputGroup>
    );
}
