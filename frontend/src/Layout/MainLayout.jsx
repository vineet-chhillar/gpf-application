import React, { useState } from "react";
import Sidebar from "./Sidebar";
import Header from "./Header";

import WithdrawlApplicationStatus from "../Pages/WithdrawlApplicationStatus";
import AdvanceApplicationStatus from "../Pages/AdvanceApplicationStatus";
import WorkflowViewerPage from "../Pages/WorkflowViewerPage";
import AppFlowState from "../Pages/AppFlowState";
import GpfWithdrawlForm from "../Pages/GpfWithdrawlForm";
import GpfAdvanceForm from "../Pages/GpfAdvanceForm";
import WithdrawlRuleMaster from "../Pages/WithdrawlRuleMaster";
import AdvanceRuleMaster from "../Pages/AdvanceRuleMaster";
import GpfWorkflowPage from "../Pages/GpfWorkflowPage";
const MainLayout = () => {
  const [activePage, setActivePage] = useState("AdminList");

  const menus = [
    
    { menuId: 1, menuName: "Withdrawl Rule Master" },
    { menuId: 2, menuName: "Advance Rule Master" },
    { menuId: 3, menuName: "Withdrawl Application Status" },
    { menuId: 4, menuName: "Advance Application Status" },
    { menuId: 5, menuName: "App WorkFlow HQ" },
    { menuId: 6, menuName: "Generate App Flow States" },
    { menuId: 7, menuName: "WithDrawl" },
    { menuId: 8, menuName: "Advance" },
    { menuId: 9, menuName: "WorkFlow/Pending For Action" },
    
    
  ];
const pageMap = {
  "Withdrawl Rule Master": <WithdrawlRuleMaster />,
  "Advance Rule Master": <AdvanceRuleMaster />,
  "Withdrawl Application Status": <WithdrawlApplicationStatus />,
  "Advance Application Status": <AdvanceApplicationStatus />,
  "App WorkFlow HQ": <WorkflowViewerPage />,
  "Generate App Flow States": <AppFlowState />,
  "WithDrawl": <GpfWithdrawlForm />,
  "Advance": <GpfAdvanceForm />,  
   "WorkFlow/Pending For Action": <GpfWorkflowPage />
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
      <div style={{ flex: 1, display: "flex", flexDirection: "column" }}>
        
        {/* HEADER */}
        <Header title={activePage} />

        {/* CONTENT (TEMP) */}
        <div style={{ padding: "0px" }}>
  {pageMap[activePage] || <h2>No page mapped</h2>}
</div>


      </div>
    </div>
  );
};

export default MainLayout;
