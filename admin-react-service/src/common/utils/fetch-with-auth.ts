import type {ContentType} from "../../components/projects/types";
import {refreshToken} from "./refresh-token";
import {getHeaders} from "./get-headers";

export async function fetchWithAuth(
    promise: (headers: Headers) => () => Promise<any>,
    contentType?: ContentType,
) {
    const token = localStorage.getItem("token");

    if (!token) {
        return Promise.reject();
    }

    if (Date.now() >= +localStorage.getItem("expires_in")!) {
        try {
            await refreshToken(localStorage.getItem("refresh_token")!);
        } catch (e) {
            return Promise.reject();
        }
    }

    const headers = await getHeaders(localStorage.getItem("token")!, contentType);
    return await promise(headers)();
}
