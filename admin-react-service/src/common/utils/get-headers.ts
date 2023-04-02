import {ContentType} from "../../components/projects/types";

function getApplication(contentType?: ContentType) {
    switch (contentType) {
        case ContentType.BLOB:
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        default:
            return "application/json";
    }
}

export function getHeaders(token: string, contentType?: ContentType) {
    const init: HeadersInit = contentType === ContentType.MEDIA ? {} :
        {
            Accept: getApplication(contentType),
            "Content-Type": getApplication(contentType)
        };

    const headers = new Headers(init);
    headers.set("Authorization", `Bearer ${token}`);
    return headers;
}
