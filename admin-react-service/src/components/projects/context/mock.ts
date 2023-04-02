import type {ProjectContextProviderType} from "./types";
import {noop} from "../../../common/utils/noop";

export const mockProjectContextValue: ProjectContextProviderType = {
    state: {
        loading: false,
        name: "",
        sharePointLink: "",
        sharePointLinkStatus: {},
        moneyPerHour: 0,
        risk: {},
        rows: [],
        columns: [],
        isShow: true,
    },
    methods: {
        updateName: noop,
        updateSharePointLink: noop,
        checkSharePointLinkStatus: noop,
        updateBySharePointLink: noop,
        handleChanges: noop,
        handleRowAdd: noop,
        handleChildRowAdd: noop,
        handleRowsReorder: noop,
        handleRowsReorderLevelUp: noop,
        handleRowsReorderLevelDown: noop,
        handleDeleteItems: noop,
        handleCanRowsReorder: noop,
        onChangeMoneyPerHour: noop,
        onChangeRisks: noop,
        handleExportProject: noop,
        loadFile: noop,
        onChangeInput: noop,
        onClickInput: noop,
        onLoadTemplate: noop,
        changeIsShow: noop,
    },
};
