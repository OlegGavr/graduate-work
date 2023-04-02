import type {CostProjectRiskDto, ApplyCostProjectTemplateVariantsDto, SharePointLinkStatusDto} from "gateway-service-api-react-client";
import type React from "react";
import type {CellChange, Column, DropPosition, Id} from "@silevis/reactgrid/src/core";
import type {ImportEnum} from "../types";
import type {UpdateCells, UpdateRow} from "../../../common/types";

export type ProjectContextProviderType = {
    state: {
        loading: boolean,
        name: string,
        sharePointLink: string,
        sharePointLinkStatus: SharePointLinkStatusDto,
        moneyPerHour: number,
        risk: CostProjectRiskDto,
        rows: UpdateRow[],
        columns: Column[],
        isShow: boolean,
    },
    methods: {
        updateName(debouncedName: string): void,
        updateSharePointLink(debouncedLink: string): void,
        checkSharePointLinkStatus(): void,
        updateBySharePointLink(): void,
        handleChanges(changes: CellChange<UpdateCells>[]): void,
        handleRowAdd(selectedRowIds: Id[]): void,
        handleChildRowAdd(selectedRowIds: Id[]): void,
        handleRowsReorder(targetRowId: Id, rowIds: Id[], dropPosition: DropPosition): void,
        handleRowsReorderLevelUp(selectedRowIds: Id[]): void,
        handleRowsReorderLevelDown(selectedRowIds: Id[]): void,
        handleDeleteItems(ids: string[]): void,
        handleCanRowsReorder(targetRowId: Id, rowIds: Id[], dropPosition: DropPosition): boolean,
        changeIsShow(value: boolean): void;
        onChangeMoneyPerHour(e: any): void,
        onChangeRisks(e: any): void,
        handleExportProject(): void,
        loadFile(type: ImportEnum): void,
        onChangeInput(e: React.ChangeEvent<HTMLInputElement>): void,
        onClickInput(e: any): void,
        onLoadTemplate(templateId: string, variant: ApplyCostProjectTemplateVariantsDto): void
    },
}
