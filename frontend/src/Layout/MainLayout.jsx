import React, { useState } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import Sidebar from "./Sidebar";
import Header from "./Header";

import GPFApplicationStatus from "../Pages/GPFApplicationStatus";
import AdvanceApplicationStatus from "../Pages/AdvanceApplicationStatus";
import WorkflowViewerPage from "../Pages/WorkflowViewerPage";
import AppFlowState from "../Pages/AppFlowState";
import GpfWithdrawlForm from "../Pages/GpfWithdrawlForm";
import GpfAdvanceForm from "../Pages/GpfAdvanceForm";
import GpfRuleMaster from "../Pages/GpfRuleMaster";
import { useNavigate } from "react-router-dom";

import GpfWorkflowInboxPage from "../Pages/GpfWorkflowInboxPage";
import GpfWorkflowDetailsPage from "../Pages/GpfWorkflowDetailsPage";
const MainLayout = () => {
  const [activePage, setActivePage] = useState("AdminList");
//const handleMenuClick = (menuName) => {
  //setActivePage(menuName);
//};
const navigate = useNavigate();

const handleMenuClick = (menuName) => {
  setActivePage(menuName);

  if (menuName === "GPF Rule Master") navigate("/rule-master");
  else if (menuName === "GPF Application Status") navigate("/status");
    else if (menuName === "WorkFlow HQ") navigate("/workflow-hq");
  else if (menuName === "WorkFlow States") navigate("/workflow-states");
  else if (menuName === "WithDrawl") navigate("/withdrawl");
  else if (menuName === "Advance") navigate("/advance");
  else if (menuName === "Pending For Action") navigate("/workflow");
};
 const menus = [
  {
    menuId: 1,
    menuName: "GPF Rule Master"
  },
   {
    menuId: 100,
    menuName: "GPF Workflow Master",
    children: [
      {
        menuId: 2,
        menuName: "WorkFlow HQ"
      },
      {
        menuId: 3,
        menuName: "WorkFlow States"
      }
    ]
  },
  {
    menuId: 200,
    menuName: "Apply For GPF",
    children: [
      {
        menuId: 4,
        menuName: "WithDrawl"
      },
      {
        menuId: 5,
        menuName: "Advance"
      }
    ]
  },
   {
    menuId: 6,
    menuName: "GPF Application Status"
  },
  //{
    //menuId: 7,
    //menuName: "Pending For Action"
  //},
  {
    menuId: 7,
    menuName: "Pending For Action"
  }
];
const pageMap = {
  "GPF Rule Master": <GpfRuleMaster />,  
  "GPF Application Status": <GPFApplicationStatus />,
  "Advance Application Status": <AdvanceApplicationStatus />,
  "WorkFlow HQ": <WorkflowViewerPage />,
  "WorkFlow States": <AppFlowState />,
  "WithDrawl": <GpfWithdrawlForm />,
  "Advance": <GpfAdvanceForm />,  
   //"Pending For Action": <GpfWorkflowPageNew />,
   "Pending For Action": <GpfWorkflowInboxPage />
,
  
};
{/*"WorkFlow/Pending For Action": <GpfWorkflowPage roleId={11} roleName="DDO" />*/}
{/*"WorkFlow/Pending For Action": <GpfWorkflowPage roleId={146} roleName="DEALING HAND(DDO)" />*/}
{/*"WorkFlow/Pending For Action": <GpfWorkflowPage roleId={144} roleName="SECTION OFFICER(Admin-2)" />*/}
{/*"WorkFlow/Pending For Action": <GpfWorkflowPage roleId={145} roleName="DEALING HAND(Admin-2)" />*/}
{/*"WorkFlow/Pending For Action": <GpfWorkflowPage roleId={144} roleName="SECTION OFFICER(Admin-2)" />*/}
{/*"WorkFlow/Pending For Action": <GpfWorkflowPage roleId={7} roleName="HO/JD" />*/}
{/*"WorkFlow/Pending For Action": <GpfWorkflowPage roleId={147} roleName="HOG" />*/}



  return (
    <div style={{ display: "flex", height: "100vh" }}>
      
      {/* LEFT SIDEBAR */}
      <Sidebar
        menus={menus}
        activePage={activePage}
        onMenuClick={handleMenuClick}
        
      />

      {/* RIGHT SIDE */}
      <div style={{ flex: 1, display: "flex", flexDirection: "column", minWidth: 0 }}>
        
        {/* HEADER */}
        <Header title={activePage} />

        {/* CONTENT (TEMP) */}
      <div
  style={{
    padding: "10px",
    background:"#f8fafc",
    height: "100%",
    overflowY: "auto",   
    overflowX: "auto"  ,
    width: "100%"  
  }}
>
<Routes>

  <Route path="/" element={<Navigate to="/rule-master" />} />

  <Route path="/rule-master" element={<GpfRuleMaster />} />
  <Route path="/status" element={<GPFApplicationStatus />} />
  <Route path="/advance-status" element={<AdvanceApplicationStatus />} />
  <Route path="/workflow-hq" element={<WorkflowViewerPage />} />
  <Route path="/workflow-states" element={<AppFlowState />} />

  <Route path="/withdrawl" element={<GpfWithdrawlForm />} />
  <Route path="/advance" element={<GpfAdvanceForm />} />

  <Route path="/workflow" element={<GpfWorkflowInboxPage />} />

  <Route
    path="/workflow/:appType/:id/:empCode"
    element={<GpfWorkflowDetailsPage />}
  />

</Routes>
</div>


      </div>
    </div>
  );
};

export default MainLayout;
