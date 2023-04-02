const WINDOW_WIDTH = window.innerWidth;
const WINDOW_HEIGHT = window.innerHeight;
const FIX_WIDTH = 340;
const TABLE_WIDTH = WINDOW_WIDTH - FIX_WIDTH;

export const nameWidth = Math.floor(TABLE_WIDTH * 0.30);
export const columnsWidth = Math.floor((TABLE_WIDTH - nameWidth) / 10);
export const tableHeight = `${WINDOW_HEIGHT}px`;

export const maxOpenCommentWidth = nameWidth * 0.75 + 10;
export const smallOpenCommentWidth = 120;
export const closeCommentWidth = 70;

export const HEIGHT_TABLE_ROW = 25;
