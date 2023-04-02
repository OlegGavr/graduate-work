import React from "react";
import {Button, Menu, MenuButton, MenuItem, MenuList} from "@chakra-ui/react";
import {ChevronDownIcon} from "@chakra-ui/icons";
import "./style.scss";

export type DropdownItemType = {
    value: string;
    action(): void;
}

type CustomDropdownProps = {
    items: DropdownItemType[],
    title: string;
    className?: string;
    disabled?: boolean;
}

export function CustomDropdown(props: CustomDropdownProps) {
    const {title, items, className, disabled} = props;

    const renderItems = () => {
        return items.map((item, index) => {
            return (
                <MenuItem key={index}
                          onClick={item.action}>
                    {item.value}
                </MenuItem>
            );
        });
    };

    return (
        <Menu>
            <MenuButton className={className}
                        as={Button}
                        disabled={disabled}
                        rightIcon={<ChevronDownIcon/>}>
                {title}
            </MenuButton>
            <MenuList>
                {renderItems()}
            </MenuList>
        </Menu>
    );
}
