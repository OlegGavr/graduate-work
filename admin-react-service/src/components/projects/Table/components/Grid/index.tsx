import React, {useContext, useEffect, useState} from "react";
import {
    ReactGrid
} from "@silevis/reactgrid/src/core";
import {confirmAlert} from "react-confirm-alert";
import type {
    CellLocation,
    Id,
    MenuOption,
    SelectionMode
} from "@silevis/reactgrid/src/core";
import type {UpdateRow} from "../../../../../common/types";
import {findChevronCell} from "../../utils";
import {ChevronHeaderCellTemplate} from "../../../../../common/components/chevron-header";
import {classnames} from "../../../../../common/utils/classnames";
import {ProjectContext} from "../../../context/context";
import "./style.scss";
import {TextCommentCellTemplate} from "../../../../../common/components/text-comment";
import {tableHeight} from "../../../../../common/constants/table";
import {CustomConfirm} from "../../../../../common/components/confirm";
import styles from "./styles.module.scss";

export function ProjectTableGrid() {
    const {state, methods} = useContext(ProjectContext);
    const {rows, columns, isShow} = state;
    const {
        handleCanRowsReorder,
        handleChanges,
        handleChildRowAdd,
        handleRowAdd,
        handleRowsReorder,
        handleRowsReorderLevelDown,
        handleRowsReorderLevelUp,
        handleDeleteItems
    } = methods;

    const [isMany, setIsMany] = useState<boolean>(false);

    useEffect(() => {
        const onContextMenu = (e: MouseEvent) => {
            setTimeout(function () {
                const contextMenuElement = document.querySelector(".reactgrid .rg-context-menu");
                if (contextMenuElement?.clientHeight && window.innerHeight - e.clientY < contextMenuElement.clientHeight) {
                    contextMenuElement?.setAttribute("style", `top: ${e.y - contextMenuElement.clientHeight}px; left: ${e.x}px; visibility: visible !important`);
                } else {
                    contextMenuElement?.setAttribute("style", `${contextMenuElement?.attributes[1].value} visibility: visible !important`);
                }
            }, 10);
        };
        window.addEventListener("contextmenu", onContextMenu);

        return () => {
            window.removeEventListener("contextmenu", onContextMenu);
        };
    }, []);

    const handleDelete = (selectedRowIds: Id[]) => {
        const hasChildren = selectedRowIds.reduce((prevValue: boolean, currentValue) => {
            const selectedRow = rows.find(row => row.rowId === currentValue);
            if (findChevronCell(selectedRow)?.hasChildren) {
                return true;
            }

            return prevValue || false;
        }, false);

        if (hasChildren) {
            setIsMany(selectedRowIds.length > 1);
            onDeleteRowWithChildren(selectedRowIds as string[]);
        } else {
            handleDeleteItems(selectedRowIds as string[]);
        }
    };

    const onDeleteRowWithChildren = (_selectedRows: string[]) => {
        confirmAlert({
            // eslint-disable-next-line react/no-unstable-nested-components
            customUI: ({onClose}: any) => {
                return (
                    <CustomConfirm header="Удаление строк"
                                   content={
                                       isMany ?
                                           "Есть зависимые строки. Вы точно хотите удалить строки со всеми зависимостями?" :
                                           "Вы точно хотите удалить строку со всеми зависимостями?"
                                   }
                                   onClick={() => {
                                       handleDeleteItems(_selectedRows);
                                   }}
                                   onClose={onClose}
                    />
                );
            }
        });
    };

    const getMenuOptions = (selectedRowIds: Id[], menuOptions: MenuOption[]): MenuOption[] => {
        if (!selectedRowIds.length) {
            return [];
        }

        if (selectedRowIds.length < 2) {
            if ((selectedRowIds[0] as string).startsWith("aggregate")) {
                return [
                    ...menuOptions,
                    {
                        id: "addRow",
                        label: "Add row",
                        handler: () => handleRowAdd(selectedRowIds)
                    },
                ];
            }

            return [
                ...menuOptions,
                {
                    id: "delete",
                    label: "Delete",
                    handler: () => handleDelete(selectedRowIds)
                },
                {
                    id: "addRow",
                    label: "Add row",
                    handler: () => handleRowAdd(selectedRowIds)
                },
                {
                    id: "addChildRow",
                    label: "Add child row",
                    handler: () => handleChildRowAdd(selectedRowIds)
                },
                {
                    id: "moveLevelUp",
                    label: "Move row level up",
                    handler: () => handleRowsReorderLevelUp(selectedRowIds)
                },
                {
                    id: "moveLevelDown",
                    label: "Move row level down",
                    handler: () => handleRowsReorderLevelDown(selectedRowIds)
                },
            ];
        }

        return [
            ...menuOptions,
            {
                id: "delete",
                label: "Delete",
                handler: () => handleDelete(selectedRowIds)
            },
        ];
    };

    const simpleHandleContextMenu = (
        selectedRowIds: Id[],
        selectedColIds: Id[],
        selectionMode: SelectionMode,
        menuOptions: MenuOption[],
        selectedRanges: CellLocation[][]
    ): MenuOption[] => {
        if (isShow) {
            return [];
        }

        if (selectionMode === "range") {
            const _selectedRows = selectedRanges.reduce((prevItem: string[], currentItem) => {
                if (currentItem.length === 1 && currentItem[0].columnId === "name") {
                    return [...prevItem, currentItem[0].rowId as string];
                }

                return prevItem;
            }, []);

            const isNameColumns = selectedRanges.length === _selectedRows.length;

            if (isNameColumns) {
                return getMenuOptions(_selectedRows, menuOptions);
            }

            return menuOptions;
        }

        if (selectionMode === "row") {
            return getMenuOptions(selectedRowIds, menuOptions);
        }
        return menuOptions;
    };

    const boldParentRows = (_rows: UpdateRow[]) => {
        return _rows.map((row) => {
                const _cell = findChevronCell(row);
                if (_cell?.hasChildren) {
                    const boldedCells = row.cells.map((cell) => {
                        return {
                            ...cell,
                            className: `${cell.className} ${styles["bolded-cell"]}`,
                        };
                    });
                    return {
                        ...row,
                        cells: boldedCells
                    };
                }
                return row;
            }
        );
    };

    return (
        <div className={classnames("project-list", styles["table-container"], {
            "shadow-cell-top": rows.length < 8
        })}
             style={{maxHeight: tableHeight}}>
            <ReactGrid rows={boldParentRows(rows)}
                       columns={columns}
                       stickyTopRows={1}
                       enableRowSelection
                       enableRangeSelection
                       stickyBottomRows={3}
                       onCellsChanged={handleChanges}
                       onRowsReordered={handleRowsReorder}
                       canReorderRows={handleCanRowsReorder}
                       onContextMenu={simpleHandleContextMenu}
                       stickyLeftColumns={3}
                       customCellTemplates={{
                           "chevron-header": new ChevronHeaderCellTemplate(),
                           "text-comment": new TextCommentCellTemplate(),
                       }}/>
        </div>
    );
}
