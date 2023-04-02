import type {AuthProvider, LegacyAuthProvider} from "react-admin";

export const authProvider: AuthProvider | LegacyAuthProvider | undefined = {
    login: async ({username, password}) => {
        const response = await fetch(`${process.env.REACT_APP_API_URL}/auth/realms/master/protocol/openid-connect/token`, {
            method: "POST",
            headers: {
                "Accept": "*/*",
                "Content-Type": "application/x-www-form-urlencoded",
            },
            body: `client_id=${process.env.REACT_APP_CLIENT_ID}&password=${password}&username=${username}&grant_type=password`
        });

        const result = JSON.parse(await response.text());

        if ("access_token" in result) {
            localStorage.removeItem("not_authenticated");
            localStorage.setItem("expires_in", String(Date.now() + +result["expires_in"] * 1000));
            localStorage.setItem("token", result["access_token"]);
            localStorage.setItem("refresh_token", result["refresh_token"]);
            localStorage.setItem("login", username);
            localStorage.setItem("user", username);
            return Promise.resolve();
        }

        localStorage.setItem("not_authenticated", "true");
        return Promise.reject();
    },
    logout: () => {
        localStorage.setItem("not_authenticated", "true");
        localStorage.removeItem("login");
        localStorage.removeItem("user");
        localStorage.removeItem("refresh_expires_in");
        localStorage.removeItem("token");
        localStorage.removeItem("refresh_token");

        return Promise.resolve();
    },
    checkError: async ({status}) => {
        if (status === 401) {
            return Promise.reject();
        }

        return Promise.resolve();
    },
    checkAuth: () => {
        return localStorage.getItem("not_authenticated")
            ? Promise.reject()
            : Promise.resolve();
    },
    getPermissions: () => {
        const role = localStorage.getItem("role");
        return Promise.resolve(role);
    },
};
