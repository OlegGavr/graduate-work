type ClassNamesObj = {
    [key: string]: boolean;
}

type ClassNames = (string | undefined | ClassNamesObj)[];

/**
 * Helper function to compute resulting className string from args.
 *
 * Usage:
 * classnames('first-classname', {
 *   'first-classname--active': shouldBeActive(),
 *   'first-classname--disabled': shouldBeDisabled(),
 * })
 *
 * Result of shouldBeActive and shouldBeDisabled must be boolean. If function returns
 * true - appropriate class will be included into resulting string
 * */
export function classnames(...classNames: ClassNames) {
    const className = Array.from(classNames)
        .reduce((result, current) => {
            if (!current) {
                return result;
            }

            return (typeof current === "string")
                ? `${result} ${current}`
                : `${result} ${computeClassnameFromObj(current)}`;
        }, "");

    return (className as string).slice(1);
}

function computeClassnameFromObj(classNamesObj: ClassNamesObj) {
    return Object.keys(classNamesObj)
        .reduce((result, currentKey) => {
            return classNamesObj[currentKey]
                ? `${result} ${currentKey}`
                : result;
        }, "")
        .slice(1);
}
