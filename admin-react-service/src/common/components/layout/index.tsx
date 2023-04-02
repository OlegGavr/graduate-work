import {Layout} from "react-admin";
import type {LayoutProps as LayoutType} from "react-admin";
import {CustomMenu} from "./menu";

export function CustomLayout(props: LayoutType) {
    return (
        <Layout {...props}
                className="custom-admin-layout"
                menu={CustomMenu}/>
    );
}
