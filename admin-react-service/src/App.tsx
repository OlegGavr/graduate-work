import React from "react";
import {Admin, Resource} from "react-admin";
import projects from "./components/projects";
import {authProvider} from "./providers/auth-provider";
import {dataProvider} from "./providers/data-provider";
import {CustomLayout} from "./common/components/layout";
import "@silevis/reactgrid/src/styles.scss";
import {LoadingContextProvider} from "./context/provider";
import {LoadingTopBar} from "./components/loading-bar";

function App() {
  return (
      <LoadingContextProvider>
          <LoadingTopBar/>
          <Admin dataProvider={dataProvider}
                 layout={CustomLayout}
                 authProvider={authProvider}>
            <Resource name="projects" {...projects}/>
          </Admin>
      </LoadingContextProvider>
  );
}

export default App;
