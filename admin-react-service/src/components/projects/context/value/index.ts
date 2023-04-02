import {useEffect, useState} from "react";
import {
    Configuration,
    CostProjectItemApi,
    CostProjectMeasureApi,
    CostProjectStructureApi,
    CostProjectExportApi,
    CostProjectImportApi,
    CostProjectApi,
    CostProjectSharepointApi
} from "gateway-service-api-react-client";
import {useToast} from "@chakra-ui/react";
import {useLocation} from "react-router-dom";
import type React from "react";
import type {CellChange, ChevronCell, DropPosition, Id, TextCell} from "@silevis/reactgrid/src/core";
import type {
    ApplyCostProjectTemplateVariantsDto,
    CostProjectItemDto, CostProjectDto
} from "gateway-service-api-react-client";
import type {ProjectContextProviderType} from "../types";
import type {UpdateCells} from "../../../../common/types";
import type {ChevronHeaderCell} from "../../../../common/components/chevron-header";
import type {
    CostProjectRiskDtoKeys,
    KeyProjectItemType
} from "../../types";
import {
    buildTree, getChildrenHeader, headerRow,
    reduceProjectAggregateRows, reduceProjectRows,
    rowsReorder, getCostItemInfo, findToReorder
} from "../../Table/utils";
import {fetchWithAuth} from "../../../../common/utils/fetch-with-auth";
import {
    ContentType,
    ImportEnum
} from "../../types";
import {getMoneyToNumber} from "../../../../common/utils/get-money";
import {PARENT_ID} from "../../../../common/constants/local-storage";
import {useStateManager} from "./state";

const configuration = new Configuration({basePath: process.env.REACT_APP_API_URL});
const costProjectApi = new CostProjectApi(configuration);
const costProjectItemApi = new CostProjectItemApi(configuration);
const costProjectMeasureApi = new CostProjectMeasureApi(configuration);
const costProjectStructureApi = new CostProjectStructureApi(configuration);
const costProjectExportApi = new CostProjectExportApi(configuration);
const costProjectImportApi = new CostProjectImportApi(configuration);
const costProjectSharepointApi = new CostProjectSharepointApi(configuration);

function getImportCostProject(type: ImportEnum) {
    switch (type) {
        case ImportEnum.AUTO:
            return costProjectImportApi.autoImport;
        case ImportEnum.HAULMONT:
            return costProjectImportApi.excelImportHaulmontCostProject;
        case ImportEnum.HSE_PLAN:
            return costProjectImportApi.excelImportHsePlanProject;
        case ImportEnum.HSE_TYPE:
            return costProjectImportApi.excelImportHseTypeProject;
    }
}

export function useProjectContextValue(): ProjectContextProviderType {
    const toast = useToast();
    const [importType, setImportType] = useState<ImportEnum>(ImportEnum.HAULMONT);
    const [loading, setLoading] = useState<boolean>(false);
    const {state, methods} = useStateManager();
    const {
        id, name, sharePointLink,
        sharePointLinkStatus, moneyPerHour,
        risk, columns, header, projects,
        rowsTree, rows
    } = state;

    const {
        refetch,
        updateComment,
        promiseHandler,
        updateName,
        updateSharePointLink,
        checkSharePointLinkStatus,
        setMoneyPerHour,
        setRisk,
        setHeader,
        setProjects,
        setAggregate,
        setRowsTree,
        setIsShow
    } = methods;
    const location = useLocation();

    useEffect(() => {
        if (location.pathname.endsWith("/show")) {
            setIsShow(true);
        } else {
            setIsShow(false);
        }
    }, []);


    const handleChanges = (changes: CellChange<UpdateCells>[]) => {
        const newRows = [...rowsTree];
        const isChangeExpanded = changes[0].type === "chevron" &&
            (changes[0].newCell as ChevronCell).isExpanded !== (changes[0].previousCell as ChevronCell).isExpanded;

        const isHeaderChange = changes[0].type === "chevron-header";

        if (isHeaderChange) {
            const newHeaderRowCells = (header.cells as ChevronHeaderCell[])
                .reduce((prevValue: ChevronHeaderCell[], currentItem: ChevronHeaderCell) => {
                    if ((currentItem as ChevronHeaderCell).chevronId === (changes[0].previousCell as ChevronHeaderCell).chevronId) {
                        if ((changes[0].newCell as ChevronHeaderCell).isExpanded) {
                            return [
                                ...prevValue,
                                ...getChildrenHeader((changes[0].newCell as ChevronHeaderCell).chevronId as KeyProjectItemType),
                                {
                                    ...currentItem,
                                    isExpanded: (changes[0].newCell as ChevronHeaderCell).isExpanded,
                                }];
                        }

                        return [
                            ...prevValue,
                            {
                                ...currentItem,
                                isExpanded: (changes[0].newCell as ChevronHeaderCell).isExpanded,
                            }
                        ];
                    }

                    const parentId = (currentItem as ChevronHeaderCell).parentId;

                    if (parentId && (parentId as string).endsWith((changes[0].previousCell as ChevronHeaderCell).chevronId as string)) {
                        if ((changes[0].newCell as ChevronHeaderCell).isExpanded) {
                            return [...prevValue, currentItem];
                        }

                        return prevValue;
                    }

                    return [...prevValue, currentItem];
                }, [] as ChevronHeaderCell[]);

            setHeader({...headerRow(), cells: newHeaderRowCells});

            return;
        }

        changes.forEach((change) => {
            const changeRowIdx = rowsTree.findIndex(el => el.rowId === change.rowId);
            const changeColumnIdx = columns.findIndex(el => el.columnId === change.columnId);
            newRows[changeRowIdx].cells[changeColumnIdx] = change.newCell;

            if (isChangeExpanded) {
                const newProjects = [...projects].map(project => {
                    if (project.id === changes[0].rowId) {
                        return {
                            ...project,
                            isExpanded: !project.isExpanded
                        };
                    }

                    return project;
                });

                setProjects(newProjects);
                return;
            }

            if (change.columnId as string === "name") {
                const defineCostItemName = (headers: Headers) =>
                    costProjectItemApi.defineCostItemName.bind(costProjectItemApi, {
                        projectId: id!,
                        itemId: change.rowId as string,
                        costProjectItemDefineNameDto: {
                            name: (change.newCell as TextCell).text,
                        }
                    }, {headers});

                promiseHandler(fetchWithAuth(defineCostItemName));
            } else if (change.columnId as string === PARENT_ID.COMMENT_CONTENT) {
                const itemId = change.rowId as string;
                const comment = (change.newCell as TextCell).text;
                updateComment(itemId, comment);
            } else {
                const projectItem = projects.find(project => project.id === change.rowId) as CostProjectItemDto;
                const costItem = getCostItemInfo(projectItem, change.columnId as string);
                const value = getMoneyToNumber((change.newCell as TextCell).text);
                let changeManualMeasureValue: (headers: Headers) => () => Promise<CostProjectDto>;

                if (value !== undefined) {
                    changeManualMeasureValue = (headers: Headers) =>
                    costProjectMeasureApi.defineManualMeasureValue.bind(costProjectMeasureApi, {
                        projectId: id!,
                        itemId: change.rowId as string,
                        measureId: costItem!.id!,
                        costProjectItemManualMeasureValueDto: {value}
                    }, {headers});
                } else {
                    changeManualMeasureValue = (headers: Headers) =>
                    costProjectMeasureApi.clearMeasureValue.bind(costProjectMeasureApi, {
                        projectId: id!,
                        itemId: change.rowId as string,
                        measureId: costItem!.id!

                    }, {headers});
                }

                promiseHandler(fetchWithAuth(changeManualMeasureValue));
            }
        });
        setRowsTree(buildTree(newRows));
    };

    const handleCanRowsReorder = (targetRowId: Id, rowIds: Id[]) => {
        return !(rowIds.length > 1 || targetRowId === "headerRow");
    };

    const handleRowsReorder = (targetRowId: Id, rowIds: Id[], dropPosition: DropPosition) => {
        if (dropPosition !== "on") {
            rowsReorder(setProjects, targetRowId, rowIds);

            const {rowId, position} = findToReorder(rowsTree, targetRowId, rowIds, dropPosition);

            const addCostItem = position === "before" ?
                costProjectStructureApi.addCostItemBefore :
                costProjectStructureApi.addCostItemAfter;

            const addCostItemHandler = (headers: Headers) =>
                addCostItem.bind(costProjectStructureApi, {
                    projectId: id!,
                    movingRequestDto: {
                        ids: rowIds as string[],
                        anchor: rowId as string,
                    }
                }, {headers});

            promiseHandler(fetchWithAuth(addCostItemHandler));
        }
    };

    const handleRowsReorderLevelUp = (selectedRowIds: Id[]) => {
        const goCostItemLevelUp = (headers: Headers) =>
            costProjectStructureApi.goCostItemLevelUp.bind(costProjectStructureApi, {
                projectId: id!,
                changeLevelRequestDto: {
                    ids: selectedRowIds as string[],
                }
            }, {headers});

        promiseHandler(fetchWithAuth(goCostItemLevelUp));
    };

    const handleRowsReorderLevelDown = (selectedRowIds: Id[]) => {
        const goCostItemLevelDown = (headers: Headers) =>
            costProjectStructureApi.goCostItemLevelDown.bind(costProjectStructureApi, {
                projectId: id!,
                changeLevelRequestDto: {
                    ids: selectedRowIds as string[],
                }
            }, {headers});

        promiseHandler(fetchWithAuth(goCostItemLevelDown));
    };

    const handleRowAdd = (selectedRowIds: Id[]) => {
        const row = projects.find(project => project.id === selectedRowIds[0]);

        const createCostItem = (headers: Headers) =>
            costProjectItemApi.createCostItem.bind(costProjectItemApi, {
                projectId: id!,
                itemId: row?.id,
                costProjectItemCreateDto: {
                    name: "New item",
                }
            }, {headers});

        promiseHandler(fetchWithAuth(createCostItem));
    };

    const handleChildRowAdd = (selectedRowIds: Id[]) => {
        const createCostSubItem = (headers: Headers) =>
            costProjectItemApi.createCostSubItem.bind(costProjectItemApi, {
                projectId: id!,
                parentId: selectedRowIds[0] as string,
                costProjectItemCreateDto: {
                    name: "New sub item"
                }
            }, {headers});

        promiseHandler(fetchWithAuth(createCostSubItem));
    };

    const handleExportProject = () => {
        setLoading(true);
        const exportCostProject = (headers: Headers) =>
            costProjectExportApi.excelExportHaulmontCostProject.bind(costProjectExportApi, {
                projectId: id,
            }, {headers});

        fetchWithAuth(exportCostProject, ContentType.BLOB)
            .then((data: Blob) => {
                const _name = projects.find(project => project.id === id)?.name;
                const link = document.createElement("a");
                link.download = _name ?? "project";
                link.href = URL.createObjectURL(data);
                link.click();
                URL.revokeObjectURL(link.href);
            })
            .finally(() => {
                setLoading(false);
            });
    };

    const handleImportProject = (file: Blob, type: ImportEnum) => {
        const rest = getImportCostProject(type);

        setLoading(true);
        const importCostProject = (headers: Headers) =>
            rest.bind(costProjectImportApi, {
                projectId: id,
                fileName: file,
            }, {headers});

        promiseHandler(
            fetchWithAuth(importCostProject, ContentType.MEDIA)
                .then((data: CostProjectDto) => {
                    refetch()
                        .then(res => {
                            setRisk(res.data.risk);
                            setProjects(reduceProjectRows(res.data, projects));
                            setAggregate(reduceProjectAggregateRows(res.data));
                            checkSharePointLinkStatus();
                        }
                    );

                    toast({description: "Файл успешно импортирован", status: "success", position: "top"});
                    return data;
                })
                .catch(() => {
                    toast({description: "Не удалось импортировать файл", status: "error", position: "top"});
                })
                .finally(() => setLoading(false))
        );
    };

    const handleDeleteItems = (ids: string[]) => {
        const deleteItems = (headers: Headers) =>
            costProjectItemApi.deleteCostItems.bind(costProjectItemApi, {
                projectId: id!,
                costProjectItemDeleteDto: {
                    ids,
                }
            }, {headers});

        promiseHandler(fetchWithAuth(deleteItems));
    };

    const onChangeMoneyPerHour = (e: any) => {
        setMoneyPerHour(+e.target.value);
    };

    const onChangeRisks = (e: any) => {
        const key = e.target.name as CostProjectRiskDtoKeys;

        setRisk((prevState: any) => {
            const newState = {...prevState};
            newState[key] = e.target.value;
            return newState;
        });
    };

    const loadFile = (type: ImportEnum) => {
        setImportType(type);
        document.getElementById("file-input")!.click();
    };

    const updateBySharePointLink = () => {
        setLoading(true);
        const updateProjectBySharePointLink = (headers: Headers) =>
            costProjectSharepointApi.updateProjectBySharePointLink.bind(costProjectSharepointApi, {
                projectId: id!
            }, {headers});

        promiseHandler(
            fetchWithAuth(updateProjectBySharePointLink)
                .then((data: CostProjectDto) => {
                    refetch().then(res => {
                            setRisk(res.data.risk);
                            setProjects(reduceProjectRows(res.data, projects));
                            setAggregate(reduceProjectAggregateRows(res.data));
                            checkSharePointLinkStatus();
                        }
                    );

                    toast({description: "Данные из Sharepoint успешно импортированы", status: "success", position: "top"});
                    return data;
                })
                .catch(() => {
                    toast({description: "Ошибка при импорте из Sharepoint", status: "error", position: "top"});
                })
                .finally(() => setLoading(false))
        );
    };

    const onChangeInput = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files) {
            handleImportProject(e.target.files[0], importType);
        }
    };

    const onClickInput = (e: any) => {
        e.target.value = null;
    };

    const onLoadTemplate = (templateId: string, variant: ApplyCostProjectTemplateVariantsDto) => {
        setLoading(true);

        const loadTemplate = (headers: Headers) =>
            costProjectApi.applyTemplate.bind(costProjectApi, {
                projectId: id!,
                applyCostProjectTemplateDto: {
                    templateId,
                    variant,
                }
            }, {headers});

        promiseHandler(fetchWithAuth(loadTemplate)
            .then(data => {
                setRisk(data.risk);
            })
            .finally(() => setLoading(false))
        );
    };

    const changeIsShow = (value: boolean) => {
        methods.setIsShow(value);
    };

    return {
        state: {
            loading,
            name,
            sharePointLink,
            sharePointLinkStatus,
            moneyPerHour,
            risk,
            rows,
            columns,
            isShow: state.isShow,
        },
        methods: {
            updateName,
            updateSharePointLink,
            checkSharePointLinkStatus,
            updateBySharePointLink,
            handleChanges,
            handleRowAdd,
            handleChildRowAdd,
            handleRowsReorder,
            handleRowsReorderLevelUp,
            handleRowsReorderLevelDown,
            handleDeleteItems,
            handleCanRowsReorder,
            onChangeMoneyPerHour,
            onChangeRisks,
            handleExportProject,
            loadFile,
            onChangeInput,
            onClickInput,
            onLoadTemplate,
            changeIsShow,
        }
    };
}
