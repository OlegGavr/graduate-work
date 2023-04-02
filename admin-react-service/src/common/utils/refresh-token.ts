export function refreshToken(_refreshToken: string) {
    return fetch(`${process.env.REACT_APP_API_URL}/auth/realms/master/protocol/openid-connect/token`, {
        method: "POST",
        headers: {
            "Accept": "*/*",
            "Content-Type": "application/x-www-form-urlencoded",
        },
        body: `client_id=${process.env.REACT_APP_CLIENT_ID}&refresh_token=${_refreshToken}&grant_type=refresh_token`
    })
        .then(async (res) => {
            if (res.status === 200) {
                const result = JSON.parse(await res.text());

                if ("access_token" in result && "refresh_token" in result) {
                    localStorage.removeItem("not_authenticated");
                    localStorage.setItem("expires_in", String(Date.now() + +result["expires_in"] * 1000));
                    localStorage.setItem("token", result["access_token"]);
                    localStorage.setItem("refresh_token", result["refresh_token"]);
                    return Promise.resolve();
                }
            }
            return Promise.reject();
        });
}
