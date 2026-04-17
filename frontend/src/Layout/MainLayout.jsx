import React, { useState } from "react";
import Sidebar from "./Sidebar";
import Header from "./Header";

import GPFApplicationStatus from "../Pages/GPFApplicationStatus";
import AdvanceApplicationStatus from "../Pages/AdvanceApplicationStatus";
import WorkflowViewerPage from "../Pages/WorkflowViewerPage";
import AppFlowState from "../Pages/AppFlowState";
import GpfWithdrawlForm from "../Pages/GpfWithdrawlForm";
import GpfAdvanceForm from "../Pages/GpfAdvanceForm";
import GpfRuleMaster from "../Pages/GpfRuleMaster";
import GpfWorkflowPage from "../Pages/GpfWorkflowPage";
import GpfWorkflowPageNew from "../Pages/GpfWorkflowPageNew";
const MainLayout = () => {
  const [activePage, setActivePage] = useState("AdminList");

  const menus = [
    
    { menuId: 1, menuName: "GPF Rule Master" },    
    { menuId: 2, menuName: "GPF Application Status" },
    { menuId: 3, menuName: "App WorkFlow HQ" },
    { menuId: 4, menuName: "App WorkFlow States" },
    { menuId: 5, menuName: "WithDrawl" },
    { menuId: 6, menuName: "Advance" },
    { menuId: 7, menuName: "Pending For Action" },
    { menuId: 8, menuName: "Pending For Action New" },
    
    
  ];
const pageMap = {
  "GPF Rule Master": <GpfRuleMaster />,  
  "GPF Application Status": <GPFApplicationStatus />,
  "Advance Application Status": <AdvanceApplicationStatus />,
  "App WorkFlow HQ": <WorkflowViewerPage />,
  "App WorkFlow States": <AppFlowState />,
  "WithDrawl": <GpfWithdrawlForm />,
  "Advance": <GpfAdvanceForm />,  
   "Pending For Action": <GpfWorkflowPage />,
   "Pending For Action New": <GpfWorkflowPageNew />
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
        onMenuClick={setActivePage}
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
  {pageMap[activePage] || <h2>No page mapped</h2>}
</div>


      </div>
    </div>
  );
};

export default MainLayout;
