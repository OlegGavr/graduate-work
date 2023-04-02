import {Configuration, CostProjectApi} from "gateway-service-api-react-client";
import type {DataProvider, LegacyDataProvider} from "react-admin";
import {createDataProvider} from "../common/utils/data-provider";
import {fetchWithAuth} from "../common/utils/fetch-with-auth";

const configuration = new Configuration({basePath: process.env.REACT_APP_API_URL});
const costProjectApi = new CostProjectApi(configuration);

export const dataProvider: DataProvider | LegacyDataProvider = createDataProvider(
    {
        getList: () => {
            const findAll = (headers: Headers) => {
                return costProjectApi.findAllCostProjects.bind(costProjectApi, {headers});
            };

            return fetchWithAuth(findAll)
                .then(data => {
                    return {
                        data: [...data],
                        total: data.length,
                    };
                });
        },
        getOne: (resource, params: { id: string }) => {
            const getOne = (headers: Headers) => {
                return costProjectApi.findCostProjectById.bind(costProjectApi, {
                    projectId: params.id
                }, {headers});
            };

            return fetchWithAuth(getOne)
                .then(data => {
                    return {data};
                });
        },
        deleteMany: (resource, params: { ids: string[] }) => {
            const deleteHandler = (headers: Headers) => {

                return costProjectApi.deleteCostProjects.bind(costProjectApi, {
                    requestBody: params.ids
                }, {headers});
            };

            return fetchWithAuth(deleteHandler)
                .then(data => {
                    return {data};
                });
        }
    }
);
