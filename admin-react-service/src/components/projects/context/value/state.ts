import {useContext, useEffect, useState} from "react";
import {useGetOne} from "react-admin";
import {useParams} from "react-router-dom";
import {
    Configuration,
    CostProjectApi,
    CostProjectSharepointApi,
    CostProjectRiskApi,
    CostProjectItemApi,
} from "gateway-service-api-react-client";
import type {
    SharePointLinkStatusDto,
    CostProjectRiskDto
} from "gateway-service-api-react-client";
import type {CostProjectRiskDtoKeys, RowItem} from "../../types";
import type {UpdateRow} from "../../../../common/types";
import {fetchWithAuth} from "../../../../common/utils/fetch-with-auth";
import {
    buildTree, getColumns, getExpandedRows,
    getRows, headerRow, reduceProjectAggregateRows,
    reduceProjectRows, getCollapsedHeader
} from "../../Table/utils";
import {getMaxWidthComment} from "../../../../common/utils/get-max-width-comment";
import {handleCommentInputFocus} from "../../../../common/utils/handle-comment-input-focus";
import {LoadingContext} from "../../../../context/context";
import {useDebounce} from "./debounce";

const configuration = new Configuration({basePath: process.env.REACT_APP_API_URL});
const costProjectApi = new CostProjectApi(configuration);
const costProjectSharepointApi = new CostProjectSharepointApi(configuration);
const costProjectItemApi = new CostProjectItemApi(configuration);
const costProjectRiskApi = new CostProjectRiskApi(configuration);

function getDefineRisk(value: CostProjectRiskDtoKeys) {
    switch (value) {
        case "_default":
            return costProjectRiskApi.defineDefaultRisk;
        case "ba":
            return costProjectRiskApi.defineBaRisk;
        case "dev":
            return costProjectRiskApi.defineDevRisk;
        case "devOps":
            return costProjectRiskApi.defineDevOpsRisk;
        case "pm":
            return costProjectRiskApi.definePmRisk;
        case "qa":
            return costProjectRiskApi.defineQaRisk;
        case "tm":
            return costProjectRiskApi.defineTmRisk;
    }
}

function getChangedFields(value: CostProjectRiskDto, prevValue: CostProjectRiskDto) {
    return (Object.keys(value) as CostProjectRiskDtoKeys[]).filter(key => value[key] !== prevValue[key]);
}

export function useStateManager() {
    const {state} = useContext(LoadingContext);
    const {id} = useParams();
    const {data, refetch} = useGetOne("projects", {id});
    const [name, setName] = useState<string>("");
    const [sharePointLink, setSharePointLink] = useState<string>("");
    const [sharePointLinkStatus, setSharePointLinkStatus] = useState<SharePointLinkStatusDto>({});
    const [moneyPerHour, setMoneyPerHour] = useState<number>(0);
    const [risk, setRisk] = useState<CostProjectRiskDto>({});
    const [isShow, setIsShow] = useState(false);

    const [columns, setColumns] = useState(getColumns([], 0));
    const [header, setHeader] = useState<UpdateRow>(headerRow());
    const [collapsedHeader, setCollapsedHeader] = useState<string[]>(getCollapsedHeader(header));
    const [projects, setProjects] = useState<RowItem[]>([]);
    const [aggregate, setAggregate] = useState<RowItem[]>([]);
    const [rowsTree, setRowsTree] = useState<UpdateRow[]>([]);
    const [rows, setRows] = useState<UpdateRow[]>([]);

    const promiseHandler = (fetch: Promise<any>) => {
        state.ref?.current?.continuousStart();
        fetch
            .finally(() => {
                refetch()
                    .then(res => {
                        setProjects(reduceProjectRows(res.data, projects));
                        setAggregate(reduceProjectAggregateRows(res.data));
                    })
                    .finally(() => {
                        state.ref?.current?.complete();
                    });
            });
    };

    const updateComment = (itemId: string, comment: string) => {
        const defineCostItemName = (headers: Headers) =>
            costProjectItemApi.defineCostItemComment.bind(costProjectItemApi, {
                projectId: id!,
                itemId,
                costProjectItemDefineCommentDto: {
                    comment
                }
            }, {headers});

        promiseHandler(fetchWithAuth(defineCostItemName));
    };

    const updateName = (debouncedName: string) => {
        if (debouncedName !== data?.name) {
            const defineCostProjectName = (headers: Headers) =>
                costProjectApi.defineCostProjectName.bind(costProjectApi, {
                    projectId: id!,
                    costProjectDefineNameDto: {
                        name: debouncedName
                    }
                }, {headers});

            fetchWithAuth(defineCostProjectName)
                .then(() => {
                    setName(debouncedName);
                    return Promise.resolve();
                })
                .catch(() => {
                    refetch().then(res => {
                            setProjects(reduceProjectRows(res.data, projects));
                            setAggregate(reduceProjectAggregateRows(res.data));
                        }
                    );
                });
        }
    };

    const updateSharePointLink = (debouncedLink: string) => {
        if (debouncedLink === data?.sharePointLink) {
            return;
        }

        const defineSharePointLink = (headers: Headers) =>
            costProjectSharepointApi.defineSharePointLink.bind(costProjectSharepointApi, {
                projectId: id!,
                costProjectSharePointLinkDto: {
                    sharePointLink: debouncedLink
                }
            }, {headers});

        fetchWithAuth(defineSharePointLink)
            .then(() => {
                setSharePointLink(debouncedLink);
                checkSharePointLinkStatus();
                return Promise.resolve();
            })
            .catch(() => {
                refetch().then(res => {
                        setProjects(reduceProjectRows(res.data, projects));
                        setAggregate(reduceProjectAggregateRows(res.data));
                    }
                );
            });
    };

    const checkSharePointLinkStatus = () => {
        const getSharePointLinkStatus = (headers: Headers) =>
            costProjectSharepointApi.checkSharePointLinkStatus.bind(costProjectSharepointApi, {
                projectId: id!
            }, {headers});

        fetchWithAuth(getSharePointLinkStatus)
            .then((res: SharePointLinkStatusDto) => {
                setSharePointLinkStatus(res);
                return Promise.resolve();
            })
            .catch(() => {
                refetch().then(res => {
                        setProjects(reduceProjectRows(res.data, projects));
                        setAggregate(reduceProjectAggregateRows(res.data));
                    }
                );
            });
    };

    const updateMoneyPerHour = (debouncedMoneyPerHour: number) => {
        if (debouncedMoneyPerHour !== data?.moneyPerHour) {
            const defineCostProjectMoneyPerHour = (headers: Headers) =>
                costProjectApi.defineCostProjectMoneyPerHour.bind(costProjectApi, {
                    projectId: id!,
                    costProjectDefineMoneyPerHourDto: {
                        moneyPerHour: debouncedMoneyPerHour
                    }
                }, {headers});

            promiseHandler(fetchWithAuth(defineCostProjectMoneyPerHour));
        }
    };

    const updateRisk = (value: CostProjectRiskDto, _prevValue: CostProjectRiskDto = {}) => {
        const keys = getChangedFields(value, _prevValue);
        keys.forEach(key => {
            const isDefaultEmpty = key === "_default" && !+value[key]!;

            if (isDefaultEmpty) {
                setRisk((prevValue: any) => {
                    return {
                        ...prevValue,
                        [key]: 0,
                    };
                });
            }

            const rest = getDefineRisk(key);
            const riskValue = isDefaultEmpty ? 0 : value[key] ? +value[key]! : undefined;

            const defineCostProjectRisk = (headers: Headers) =>
                rest.bind(costProjectRiskApi, {
                    projectId: id!,
                    costProjectDefineRiskDto: {
                        riskValue: riskValue
                    }
                }, {headers});

            promiseHandler(fetchWithAuth(defineCostProjectRisk));
        });
    };

    useDebounce<number>(moneyPerHour, 500, updateMoneyPerHour);
    useDebounce<CostProjectRiskDto>(risk, 500, updateRisk);

    useEffect(() => {
        if (data) {
            setName(data?.name ?? "");
            setSharePointLink(data?.sharePointLink ?? "");
            setMoneyPerHour(data?.moneyPerHour ?? 0);
            setRisk(data?.risk ?? {});
            setProjects(reduceProjectRows(data, projects));
            setAggregate(reduceProjectAggregateRows(data));
        }
    }, [data]);

    useEffect(() => {
        setCollapsedHeader(prevState => {
            const updateCollapsedHeader = getCollapsedHeader(header);
            if (updateCollapsedHeader.length >= prevState.length) {
                return updateCollapsedHeader;
            } else {
                setColumns(getColumns(updateCollapsedHeader, getMaxWidthComment(rowsTree)));
                return updateCollapsedHeader;
            }
        });
    }, [header]);

    useEffect(() => {
        const getProjects = projects.length ? projects : [];
        const getAggregate = aggregate.length ? aggregate : [];
        const rowsProject = [...getProjects, ...getAggregate];

        const expandedCommentColumn = (rowId: string) => {
            const row = rows.find(item => item.rowId === rowId);
            handleCommentInputFocus(rows, row!);
        };

        setRowsTree(buildTree(getRows(rowsProject, collapsedHeader, isShow, expandedCommentColumn)));
    }, [collapsedHeader, projects, aggregate]);

    useEffect(() => {
        setRows([
            header,
            ...getExpandedRows(rowsTree),
        ]);
    }, [header, rowsTree]);

    useEffect(() => {
        setColumns(getColumns(collapsedHeader, getMaxWidthComment(rowsTree)));
    }, [rowsTree]);

    return {
        state: {
            id,
            name,
            sharePointLink,
            sharePointLinkStatus,
            moneyPerHour,
            risk,
            columns,
            header,
            projects,
            rowsTree,
            rows,
            isShow,
        },
        methods: {
            refetch,
            updateName,
            updateSharePointLink,
            checkSharePointLinkStatus,
            setMoneyPerHour,
            setRisk,
            setHeader,
            setProjects,
            setAggregate,
            setRowsTree,
            updateComment,
            promiseHandler,
            setIsShow,
        },
    };
}
