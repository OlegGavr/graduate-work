import React, {useContext, useEffect, useState} from "react";
import {
    Configuration,
    CostProjectTemplateApi,
} from "gateway-service-api-react-client";
import {confirmAlert} from "react-confirm-alert";
import type {CostProjectTemplateDto} from "gateway-service-api-react-client";
import type {DropdownItemType} from "../../../../../common/components/dropdown";
import {CustomDropdown} from "../../../../../common/components/dropdown";
import {fetchWithAuth} from "../../../../../common/utils/fetch-with-auth";
import {ProjectContext} from "../../../context/context";
import "./style.scss";
import {TemplateConfirm} from "./TemplateConfirm";

const configuration = new Configuration({basePath: process.env.REACT_APP_API_URL});
const costProjectTemplateApi = new CostProjectTemplateApi(configuration);

export function ProjectTableTemplate() {
    const {state, methods} = useContext(ProjectContext);
    const {onLoadTemplate} = methods;

    const [templateItems, setTemplateItems] = useState<DropdownItemType[]>([]);

    useEffect(() => {
        const findAllCostProjectTemplates = (headers: Headers) =>
            costProjectTemplateApi.findAllCostProjectTemplates.bind(costProjectTemplateApi, {headers});

        fetchWithAuth(findAllCostProjectTemplates)
            .then((res: CostProjectTemplateDto[]) => {
                const newTemplateItems = res.map(template => {
                    return {
                        value: template.name,
                        action: () => {
                            openConfirm(template.id!);
                        }
                    };
                }) as DropdownItemType[];

                setTemplateItems(newTemplateItems);
            });
    }, []);

    const openConfirm = (_templateId: string) => {
        confirmAlert({
            // eslint-disable-next-line react/no-unstable-nested-components
            customUI: ({onClose}: any) => {
                return <TemplateConfirm onClose={onClose} onLoadTemplate={onLoadTemplate} templateId={_templateId}/>;
            }
        });
    };

    return (
        <CustomDropdown title="Загрузить шаблон"
                        items={templateItems}
                        disabled={state.isShow}/>
    );
}
