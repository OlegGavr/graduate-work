import {CostProjectItemDetailMeasureTypeDto} from "gateway-service-api-react-client";
import type {DefaultCellTypes} from "@silevis/reactgrid/src/core";
import type {RowItem} from "../../../types";
import type {KeyCostProjectItemType} from "../../../../../common/types";
import {getNumberToMoney} from "../../../../../common/utils/get-money";
import {classnames} from "../../../../../common/utils/classnames";
import styles from "../styles.module.scss";

type GroupIdType = `${KeyCostProjectItemType}.`

function getString(num?: number) {
    return num || num === 0 ? String(num) : "";
}

function renderCell(
    text: string,
    type?: CostProjectItemDetailMeasureTypeDto
) {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const className = type === CostProjectItemDetailMeasureTypeDto.Manual ?
        `${styles.cell} ${styles.manual}` : styles.cell;

    return (
        <div className={styles.cell}>
            {getNumberToMoney(text)}
        </div>
    );
}

export function renderCells(
    project: RowItem,
    groupId: GroupIdType,
    isAggregate: boolean,
    isShadow: boolean,
    isHighlight: boolean,
    background: string,
): DefaultCellTypes[] {
    const realGroupId = groupId.slice(0, -1) as KeyCostProjectItemType;
    return [
        {
            type: "text",
            groupId: `develop.${groupId}`,
            text: getString(project[realGroupId]?.backendCost?.value),
            nonEditable: isAggregate,
            className: classnames({
                "red-cell": isHighlight,
                "shadow-cell-bottom": isShadow
            }),
            renderer: text => renderCell(text, project[realGroupId]?.backendCost?.type),
        },
        {
            type: "text",
            groupId: `develop.${groupId}`,
            text: getString(project[realGroupId]?.frontendCost?.value),
            nonEditable: isAggregate,
            className: classnames({
                "red-cell": isHighlight,
                "shadow-cell-bottom": isShadow
            }),
            renderer: text => renderCell(text, project[realGroupId]?.frontendCost?.type),
        },
        {
            type: "text",
            groupId: groupId,
            text: getString(project[realGroupId]?.devCost?.value),
            nonEditable: isAggregate,
            className: classnames({
                "red-cell": isHighlight,
                "shadow-cell-bottom": isShadow
            }),
            renderer: text => renderCell(text, project[realGroupId]?.devCost?.type),
        },
        {
            type: "text",
            groupId: groupId,
            text: getString(project[realGroupId]?.qaCost?.value),
            nonEditable: isAggregate,
            className: classnames({
                "red-cell": isHighlight,
                "shadow-cell-bottom": isShadow
            }),
            renderer: text => renderCell(text, project[realGroupId]?.qaCost?.type),
        },
        {
            type: "text",
            groupId: groupId,
            text: getString(project[realGroupId]?.analyseCost?.value),
            nonEditable: isAggregate,
            className: classnames({
                "red-cell": isHighlight,
                "shadow-cell-bottom": isShadow
            }),
            renderer: text => renderCell(text, project[realGroupId]?.analyseCost?.type),
        },
        {
            type: "text",
            groupId: groupId,
            text: getString(project[realGroupId]?.devOpsCost?.value),
            nonEditable: isAggregate,
            className: classnames({
                "red-cell": isHighlight,
                "shadow-cell-bottom": isShadow
            }),
            renderer: text => renderCell(text, project[realGroupId]?.devOpsCost?.type),
        },
        {
            type: "text",
            groupId: groupId,
            text: getString(project[realGroupId]?.tmCost?.value),
            nonEditable: isAggregate,
            className: classnames({
                "red-cell": isHighlight,
                "shadow-cell-bottom": isShadow
            }),
            renderer: text => renderCell(text, project[realGroupId]?.tmCost?.type),
        },
        {
            type: "text",
            groupId: groupId,
            text: getString(project[realGroupId]?.pmCost?.value),
            nonEditable: isAggregate,
            className: classnames({
                "red-cell": isHighlight,
                "shadow-cell-bottom": isShadow
            }),
            renderer: text => renderCell(text, project[realGroupId]?.pmCost?.type),
        },
        {
            type: "text",
            groupId: `sum${groupId[0].toUpperCase() + groupId.slice(1)}`,
            text: getString(project[realGroupId]?.sumCost?.value),
            style: {background},
            nonEditable: isAggregate,
            className: classnames({
                "red-cell": isHighlight,
                "shadow-cell-bottom": isShadow || project.id!.startsWith("aggregate0")
            }),
            renderer: text => renderCell(text, project[realGroupId]?.sumCost?.type),
        },
    ];
}
