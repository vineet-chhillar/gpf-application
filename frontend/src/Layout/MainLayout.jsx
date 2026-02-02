import React, { useState } from "react";
import Sidebar from "./Sidebar";
import Header from "./Header";
import StatusMasterPage from "../Pages/StatusMaster/StatusMasterPage";
import WithdrawlApplicationStatus from "../Pages/WithdrawlApplicationStatus";
import AdvanceApplicationStatus from "../Pages/AdvanceApplicationStatus";
import AppFlowHQ from "../Pages/AppFlowHQ";
import AppFlowState from "../Pages/AppFlowState";
import GpfWithdrawlForm from "../Pages/GpfWithdrawlForm";
import Advance from "../Pages/Advance";
import WithdrawlRuleMaster from "../Pages/WithdrawlRuleMaster";
import AdminWithdrawalInbox from "../Pages/AdminWithdrawalInbox"; 


const MainLayout = () => {
  const [activePage, setActivePage] = useState("AdminList");

  const menus = [
    { menuId: 1, menuName: "Status Master" },
    { menuId: 2, menuName: "Withdrawl Rule Master" },
    { menuId: 3, menuName: "Withdrawl Application Status" },
    { menuId: 4, menuName: "Advance Application Status" },
    { menuId: 5, menuName: "Generate App Flow HQ" },
    { menuId: 6, menuName: "Generate App Flow States" },
    { menuId: 7, menuName: "WithDrawl" },
    { menuId: 8, menuName: "Advance" },
    { menuId: 9, menuName: "Admin Withdrawl Inbox" },
    
  ];
const pageMap = {
  "Status Master": <StatusMasterPage />,
  "Withdrawl Rule Master": <WithdrawlRuleMaster />,
  "Withdrawl Application Status": <WithdrawlApplicationStatus />,
  "Advance Application Status": <AdvanceApplicationStatus />,
  "Generate App Flow HQ": <AppFlowHQ />,
  "Generate App Flow States": <AppFlowState />,
  "WithDrawl": <GpfWithdrawlForm />,
  "Advance": <Advance />,
  "Admin Withdrawl Inbox": <AdminWithdrawalInbox />,
  
};




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
        <div style={{ padding: "20px" }}>
  {pageMap[activePage] || <h2>No page mapped</h2>}
</div>


      </div>
    </div>
  );
};

export default MainLayout;
