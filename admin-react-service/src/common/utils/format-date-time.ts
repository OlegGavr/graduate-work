export function formatDateTime(
  value: string | number | Date | undefined,
  options: Intl.DateTimeFormatOptions = {year: "numeric", month: "numeric", day: "numeric", hour: "numeric", minute: "numeric"}
): string | null {
  if (!value) {
    return null;
  }

  const date = new Date(value);
  return new Intl.DateTimeFormat("ru-Ru", options).format(date);
}
