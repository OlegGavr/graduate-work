import React, { createContext, FC } from "react";
import { State } from "../Model/State";

const StateContext = createContext({} as State);

export const StateProvider: FC<{ children: React.ReactNode, state: State }> = ({ children, state }) => (
  <StateContext.Provider value={state}>{children}</StateContext.Provider>
);

export const useReactGridState = (): State => React.useContext(StateContext);
