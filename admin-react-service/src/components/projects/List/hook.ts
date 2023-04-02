import {useNavigate} from "react-router-dom";
import {Configuration, CostProjectApi} from "gateway-service-api-react-client";
import {fetchWithAuth} from "../../../common/utils/fetch-with-auth";

export function useCreateProject() {
    const navigate = useNavigate();
    const configuration = new Configuration({basePath: process.env.REACT_APP_API_URL});
    const costProjectApi = new CostProjectApi(configuration);

    return () => {
        const createProject = (headers: Headers) => {
            return costProjectApi.createEmptyCostProject.bind(costProjectApi,{headers});
        };

        return fetchWithAuth(createProject)
            .then(data => {
                navigate(data.id + "/show");
            });
    };
}
