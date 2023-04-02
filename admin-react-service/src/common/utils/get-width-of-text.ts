import type {StylesType} from "../types";

type SizeInfo = {
    width: number,
    height: number
}

export function getWidthOfText(
    txt: string,
    styles: StylesType,
    //white-space?: nowrap,
    ): SizeInfo {
    const {paddingLeft, maxWidth, width, whiteSpace} = styles;

    const name = document.getElementById("width-name");
    name!.style.paddingLeft = paddingLeft ?? "0";
    name!.style.width = width ?? "auto";
    name!.style.maxWidth = maxWidth ?? "auto";
    name!.style.whiteSpace = whiteSpace ?? "normal";

    if (name) {
        name.innerHTML = txt;
        console.log(name?.offsetWidth, name?.offsetHeight);

        return {
            width: name?.offsetWidth,
            height: name?.offsetHeight,
        };
    }

    return {
        width: 0,
        height: 0,
    };
}
