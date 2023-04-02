export function getNumberToMoney(num: string) {
    return num ? parseFloat(num)
        .toFixed(2)
        .replace(/(\d)(?=(\d{3})+\.)/g, "$1 ") : "";
}

export function getMoneyToNumber(money: string) {
    if (!money) {
        return undefined;
    }
    return +money.replace(/\s+/g, "");
}
