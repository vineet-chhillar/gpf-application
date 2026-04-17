import React, { useState, useEffect } from "react";
import api from "../api/axios";
import "../styles/GpfWorkflowPageworkflow.css";
import RuleDetailsSection from "./RuleDetailsSection";
const GpfWorkflowPage = () => {

const [applications, setApplications] = useState([]);
const [selectedId, setSelectedId] = useState(null);
const [actions, setActions] = useState([]);
const [selectedAction, setSelectedAction] = useState("");
const [remarks, setRemarks] = useState("");
const [expandedRow, setExpandedRow] = useState(null);
const [trailMap, setTrailMap] = useState({});
const [detailsMap, setDetailsMap] = useState({});
const [appType, setAppType] = useState("withdrawl");
const [sendToOptions, setSendToOptions] = useState([]);
const [selectedSendTo, setSelectedSendTo] = useState("");
const [editData, setEditData] = useState({});
const [nextRoleName, setNextRoleName] = useState("");
const [advanceRules, setAdvanceRules] = useState([]);
const [withdrawlRules, setWithdrawlRules] = useState([]);

useEffect(() => {
  api.get("/gpf/withdrawal-rules/active")
    .then(res => setWithdrawlRules(res.data))
    .catch(err => console.error(err));
}, []);


useEffect(() => {
  api.get("/gpf/advance-rules/active")
    .then(res => {
      console.log("RULES:", res.data); // 👈 ADD THIS
      setAdvanceRules(res.data);
    })
    .catch(err => console.error("Rule load error", err));
}, []);
const toggleExpand = async (applicationId, empCode) => {

if (expandedRow === applicationId) {
setExpandedRow(null);
return;
}

try {

const base =
appType === "withdrawl"
? "/gpf-withdrawl"
: "/gpf-advance";

const [trailRes, detailsRes] = await Promise.all([
api.get(`${base}/trail/${applicationId}`),
api.get(`${base}/status/${empCode}`)
]);

setTrailMap(prev => ({
...prev,
[applicationId]: trailRes.data
}));

setDetailsMap(prev => ({
...prev,
[applicationId]: detailsRes.data
}));
const details = detailsRes.data.details;

setEditData(prev => ({
  ...prev,
  [applicationId]: {
    ...details,
    advancerule: Number(details.advanceruleId ?? details.advancerule ?? 0),
    ruleSpecificDataJson: details.ruleSpecificDataJson || {}
  }
}));
} catch (err) {
console.error("Expand load error", err);
}

setExpandedRow(prev => prev === applicationId ? null : applicationId);
};
const handleEditChange = (appId, field, value) => {

  setEditData(prev => {

    const existing = prev[appId] || {};

    // 🔥 RULE CHANGE HANDLING (NO HARDCODING)
 if (field === "advancerule") {
  return {
    ...prev,
    [appId]: {
      ...existing,
      advancerule: value,
      ruleSpecificDataJson: {}   // reset only
    }
  };
}

    // ✅ NORMAL + JSON MERGE
    if (field === "ruleSpecificDataJson") {
      return {
        ...prev,
        [appId]: {
          ...existing,
          ruleSpecificDataJson: value
        }
      };
    }
// ✅ define updated FIRST
    let updated = {
      ...existing,
      [field]: value
    };
console.log(editData[appId]);
    // ✅ then use it
   if (field === "amountofadvancerequested") {

      const requested = Number(value) || 0;

      // ✅ FALLBACK TO EXISTING OR ORIGINAL DATA
      const outstanding =
        Number(existing.amountofadvanceoutstanding) ||
        Number(applications.find(a => a.applicationId === appId)?.amountofadvanceoutstanding) ||
        0;

      updated.amountofconsolidatedadvance =
        requested + outstanding;
    }
    return {
      ...prev,
      [appId]: updated   // ✅ CLEAN
    };
  });
};
useEffect(() => {

  if (!selectedId) {
    setNextRoleName("");
    return;
  }

 
  if (!selectedApp) return;

  const details = detailsMap[selectedApp.applicationId]?.details;

  if (!details?.workflowId || !details?.currentStep) return;

  const fetchNextRole = async () => {
    try {

      const res = await api.get(
        `/workflow/transitions/${details.workflowId}`
      );

      const transitions = res.data;

      const next = transitions.find(
        t => t.stepOrder === details.currentStep + 1
      );

      if (next) {
        setNextRoleName(next.toRole);
      } else {
        setNextRoleName("Completed");
      }

    } catch (err) {
      console.error("Next role fetch failed", err);
    }
  };

  fetchNextRole();

}, [selectedId, detailsMap, applications]);

{/*useEffect(() => {
  if (!selectedId) return;

  const app = applications.find(
    a => Number(a.applicationId) === Number(selectedId)
  );

  if (app) {
    toggleExpand(app.applicationId, app.empCode);
  }
}, [selectedId, applications]);*/}

// 🔥 add applications
console.log("Applications:", applications);
console.log("Looking for ID:", selectedId);
useEffect(() => {

if (!selectedId) {
  {/*setSendToOptions([]);*/}
   setSendToOptions([]);
  setSelectedSendTo("");
  return;
}

const app = applications.find(
  a => Number(a.applicationId) === Number(selectedId)
);

if (!app) return;

console.log("Selected App:", app);

/* ✅ USE ROLE FROM INBOX */
{/*if (![60, 146].includes(selectedApp.currentOwnerRoleId)) {
  console.log("Role not eligible");
  setSendToOptions([]);
  return;
}*/}

const fetchSendToRoles = async () => {
  try {

    const base =
      appType === "withdrawl"
        ? "/gpf-withdrawl"
        : "/gpf-advance";

    const res = await api.get(
      `${base}/status/${app.empCode}`
    );

    const details = res.data.details;

    console.log("DETAILS:", details);

    if (!details?.workflowId || !details?.currentStep) {
      console.warn("Missing workflow data");
      return;
    }

    // 🔥 RETURN CASE (HANDLE FIRST)
    if (details.isReturned && details.returnFromStep != null) {

      console.log("🔥 RETURN MODE");

      const roleRes = await api.get(
        `/workflow/role-by-step/${details.workflowId}/${details.returnFromStep}`
      );

      setSendToOptions([roleRes.data]); // only one role

      return; // ❗ STOP here
    }

    // ✅ NORMAL FLOW
    const rolesRes = await api.get(
      `/workflow/previous-roles/${details.workflowId}/${details.currentStep}`
    );

    setSendToOptions(rolesRes.data);

  } catch (err) {
    console.error("SendTo load error", err);
  }
};

fetchSendToRoles();

}, [selectedId, applications, appType]);
/* ================= LOAD INBOX ================= */

useEffect(() => {

const url =
appType === "withdrawl"
? "/gpf-withdrawl/inbox"
: "/gpf-advance/inbox";

api.get(url)
.then(res => {
setApplications(res.data);
setExpandedRow(null);
})
.catch(err => console.error("Inbox load error", err));

}, [appType]);

/* ================= LOAD ACTIONS ================= */

useEffect(() => {
api.get("/gpf-withdrawl/actions")
.then(res => {
setActions(Array.isArray(res.data) ? res.data : []);
})
.catch(err => console.error("Action load error", err));
}, []);

/* ================= CHECKBOX ================= */

const handleSelection = (id) => {
  setSelectedId(id);
};


const formatLabel = (key) => {
  return key
    .replace(/([A-Z])/g, " $1")
    .replace(/^./, str => str.toUpperCase());
};
/* ================= PROCESS ================= */

const handleProcess = async () => {

if (!selectedId) {
alert("Select at least one application");
return;
}

if (!selectedAction) {
alert("Select action");
return;
}

try {

const url =
appType === "withdrawl"
? "/gpf-withdrawl/process"
: "/gpf-advance/process";

await api.post(url, {
applicationIds: [selectedId],
actionId: selectedAction,
remarks,
sendToRole: selectedSendTo || null
});

alert("Processed successfully");

const reloadUrl =
appType === "withdrawl"
? "/gpf-withdrawl/inbox"
: "/gpf-advance/inbox";

const res = await api.get(reloadUrl);
setApplications(res.data);

setTimeout(() => {
  setSelectedId(null);
}, 0);


setSelectedAction("");
setRemarks("");


setSendToOptions([]);     
setSelectedSendTo("");    
setDetailsMap({});        
setTrailMap({});          


} catch (err) {
alert(err.response?.data || "Processing failed");
}

};
const getButtonText = () => {

  // 🔥 NEW: Cancel / Reject case
  if (String(selectedAction) === "12") {
    return "Cancel (Reject)";
  }

  // 🔁 Return mode
  const isReturnMode =
    selectedDetails?.isReturned && selectedDetails?.returnFromStep != null;

  if (selectedSendTo || isReturnMode) {
    const role = sendToOptions.find(
      r => String(r.roleId) === String(selectedSendTo)
    );

    return `Send To ${role?.roleName || ""}`;
  }

  // ✅ Normal flow
  if (nextRoleName) {
    return `Send To ${nextRoleName}`;
  }

  return "Process Selected";
};

const selectedApp = applications.find(
  a => Number(a.applicationId) === Number(selectedId)
);

const selectedDetails =
  selectedApp
    ? detailsMap[selectedApp.applicationId]?.details
    : null;

    const details = selectedDetails || {};
const isRowEditable = details?.isReturned === true;
const master =
  selectedApp
    ? detailsMap[selectedApp.applicationId]?.master || {}
    : {};

    
useEffect(() => {

  if (selectedDetails?.isReturned && sendToOptions.length === 1) {

    console.log("Auto selecting return role");

    setSelectedSendTo(sendToOptions[0].roleId);

  }

}, [sendToOptions, selectedDetails]);


  
    console.log("selectedSendTo:", selectedSendTo);
console.log("sendToOptions:", sendToOptions);

const handleUpdate = async (appId) => {

  try {

    const base =
      appType === "withdrawl"
        ? "/gpf-withdrawl"
        : "/gpf-advance";

    const payload = editData[appId];

    await api.put(`${base}/update/${appId}`, {
  details: payload,
  ruleSpecificData: payload.ruleSpecificDataJson   // 🔥 IMPORTANT FOR ADVANCE
});

    alert("Updated successfully");

    // reload details
    const app = applications.find(a => a.applicationId === appId);
    if (app) {
      toggleExpand(app.applicationId, app.empCode);
    }

  } catch (err) {
    alert("Update failed");
  }
};
const handleRuleFieldChange = (appId, field, value) => {
  setEditData(prev => ({
    ...prev,
    [appId]: {
      ...prev[appId],
      ruleSpecificDataJson: {
        ...(prev[appId]?.ruleSpecificDataJson || {}),
        [field]: value
      }
    }
  }));
};

return (
  
<div className="workflow-container">

{/* TYPE SELECTOR */}
<div className="type-selector">
  <label className={appType === "withdrawl" ? "active" : ""}>
    <input type="radio" checked={appType === "withdrawl"} onChange={() => setAppType("withdrawl")} />
    <span>Withdrawal</span>
  </label>

  <label className={appType === "advance" ? "active" : ""}>
    <input type="radio" checked={appType === "advance"} onChange={() => setAppType("advance")} />
    <span>Advance</span>
  </label>
</div>


{/* ================= MAIN LAYOUT ================= */}
<div className="workflow-layout">

  {/* LEFT LIST */}
<div className="workflow-list">
  <div className="workflow-table-wrapper">

    <table className="workflow-table">
      <thead>
        <tr>
          <th>Select</th>
          
<th>Emp Code</th>
<th>Pending With</th>
<th>Name</th>
<th>Designation</th>
<th>Amount</th>
<th>Date</th>
<th>Purpose</th>
<th>App ID</th>
        </tr>
      </thead>

      <tbody>
        {applications.map(app => (
          <tr
  key={app.applicationId}
  className={selectedId === app.applicationId ? "selected-row" : ""}
  onClick={() => {
  setSelectedId(app.applicationId);
  toggleExpand(app.applicationId, app.empCode);
}}
>

  {/* RADIO */}
  <td>
    {app.pendingWithRole !== "Completed" && (
      <input
        type="radio"
        name="applicationSelect"
        checked={selectedId === app.applicationId}
        onChange={(e) => {
          e.stopPropagation();
          setSelectedId(app.applicationId);
        }}
      />
    )}
  </td>

  
  <td>{app.empCode}</td>
  <td>{app.pendingWithRole}</td>
  <td>{app.employeeName}</td>
  <td>{app.designation}</td>
  <td>₹{app.amount}</td>

  <td>
    {app.applicationDate
      ? new Date(app.applicationDate).toLocaleDateString()
      : ""}
  </td>

  <td className="purpose-cell">{app.purpose}</td>
  <td>{app.applicationId}</td>

</tr>
        ))}
      </tbody>
    </table>
  </div>
</div>

  {/* RIGHT DETAILS */}
<div className="workflow-details">

  {!selectedId && <p>Select an application</p>}

  {selectedId && selectedDetails && (

    <div className="expanded-content">

     <div className="top-panel">
  {/*<h4>Status Trail</h4>*/}

<div className="trail-horizontal">
  {trailMap[selectedId]?.length > 0 ? (
    trailMap[selectedId].map((t, i) => (
      <div
        key={i}
        className={`trail-step ${
          i === trailMap[selectedId].length - 1 ? "active" : "completed"
        }`}
      >
        <span className="trail-chip">
          <span className="role">{t.role}</span>
          <span className="action">{t.action}</span>
          <span className="time">{t.time || t.actionAt}</span>
        </span>
      </div>
    ))
  ) : (
    <p>No history</p>
  )}
</div>
</div>


      {/* LEFT PANEL */}
      <div className="left-panel">

        {/* ================= EMPLOYEE ================= */}
        <div className="section-block">
          <h4>Employee Details</h4>
          <div className="details-grid details-grid-4">
            <div className="detail-item"><span>Code</span><b>{master?.empcode}</b></div>
            <div className="detail-item"><span>Name</span><b>{master?.empname}</b></div>
            <div className="detail-item"><span>Designation</span><b>{master?.designation}</b></div>
            <div className="detail-item"><span>Division</span><b>{master?.empdivision}</b></div>
            <div className="detail-item"><span>Mobile</span><b>{master?.empmobileno}</b></div>
            <div className="detail-item"><span>Email</span><b>{master?.empemailid}</b></div>
            <div className="detail-item"><span>Joining</span><b>{master?.dateofjoining}</b></div>
            <div className="detail-item"><span>Retirement</span><b>{master?.dateofsuperannuation}</b></div>
          </div>
        </div>

        {/* ================= GPF SUMMARY ================= */}
        <div className="section-block">
          <h4>GPF Summary</h4>
          <div className="details-grid details-grid-4">
            <div className="detail-item"><span>Account No</span><b>{details?.gpfaccountno}</b></div>
            <div className="detail-item"><span>Basic Pay</span><b>₹{details?.basicpay}</b></div>
            <div className="detail-item"><span>Balance Date</span><b>{details?.dateofoutstandingbalance}</b></div>
            <div className="detail-item"><span>Balance</span><b>₹{details?.outstandingbalance}</b></div>
            <div className="detail-item"><span>Credit From</span><b>{details?.creditfromdate}</b></div>
            <div className="detail-item"><span>Credit To</span><b>{details?.credittodate}</b></div>
            <div className="detail-item"><span>Total Credit</span><b>₹{details?.totalcreditamount}</b></div>
            <div className="detail-item"><span>Total Refund</span><b>₹{details?.refundafterdateofoutstandingbalance}</b></div>
          </div>
        </div>

        {/* ================= BALANCE ================= */}
        <div className="section-block">
          <h4>Balance Calculation</h4>
          <div className="details-grid details-grid-4">
            <div className="detail-item"><span>Withdraw From</span><b>{details?.withdrawlfromdate}</b></div>
            <div className="detail-item"><span>Withdraw To</span><b>{details?.withdrawltodate}</b></div>
            <div className="detail-item"><span>Total Withdrawal</span><b>₹{details?.totalwithdrawlamount}</b></div>
            <div className="detail-item critical"><span>Net Balance</span><b>₹{details?.netbalance}</b></div>
          </div>
        </div>

        {/* ================= WITHDRAWAL ================= */}
       {appType === "withdrawl" && (
  <div className="section-block">

    <h4>Withdrawal Request</h4>

    <div className="details-grid details-grid-4">

      {/* Requested Amount */}
      <div className="detail-item critical">
        <span>Requested</span>

        {isRowEditable ? (
          <input
            value={editData[selectedId]?.amountofwithdrawlrequested || ""}
            onChange={(e) =>
              handleEditChange(
                selectedId,
                "amountofwithdrawlrequested",
                e.target.value
              )
            }
          />
        ) : (
          <b>₹{details?.amountofwithdrawlrequested}</b>
        )}
      </div>

      {/* Purpose */}
      <div className="detail-item">
        <span>Purpose</span>

        {isRowEditable ? (
          <input
            value={editData[selectedId]?.purposeofwithdrawl || ""}
            onChange={(e) =>
              handleEditChange(
                selectedId,
                "purposeofwithdrawl",
                e.target.value
              )
            }
          />
        ) : (
          <b>{details?.purposeofwithdrawl}</b>
        )}
      </div>

      {/* 🔥 RULE DROPDOWN */}
      <div className="detail-item critical">
        <span>Rule</span>

        {isRowEditable ? (
          <select
            value={
              editData[selectedId]?.withdrawlrule ??
              details?.withdrawlrule ??
              ""
            }
            onChange={(e) =>
              handleEditChange(
                selectedId,
                "withdrawlrule",
                Number(e.target.value)
              )
            }
          >
            <option value="">-- Select Rule --</option>

            {withdrawlRules.map(rule => (
              <option key={rule.ruleId} value={rule.ruleId}>
                {rule.withdrawlReason}
              </option>
            ))}
          </select>
        ) : (
          <b>{details?.withdrawlruleText}</b>
        )}
      </div>

      {/* Officer */}
      <div className="detail-item">
        <span>Officer</span>

        {isRowEditable ? (
          <input
            value={editData[selectedId]?.concernedofficername || ""}
            onChange={(e) =>
              handleEditChange(
                selectedId,
                "concernedofficername",
                e.target.value
              )
            }
          />
        ) : (
          <b>{details?.concernedofficername}</b>
        )}
      </div>

      {/* Date (readonly always) */}
      <div className="detail-item">
        <span>Date</span>
        <b>{details?.dateofapplication}</b>
      </div>

    </div>

    {/* ================= PREVIOUS WITHDRAWAL ================= */}

    <h4>Previous Withdrawal</h4>

    <div className="details-grid details-grid-4">

      {/* Checkbox */}
      <div className="detail-item">
        <span>Prior</span>

        {isRowEditable ? (
          <input
            type="checkbox"
            checked={
              editData[selectedId]?.ispriorwithdrawlforsamepurpose || false
            }
            onChange={(e) =>
              handleEditChange(
                selectedId,
                "ispriorwithdrawlforsamepurpose",
                e.target.checked
              )
            }
          />
        ) : (
          <b>
            {details?.ispriorwithdrawlforsamepurpose ? "Yes" : "No"}
          </b>
        )}
      </div>

      {/* Amount (conditional) */}
      <div className="detail-item">
        <span>Amount</span>

        {isRowEditable &&
        editData[selectedId]?.ispriorwithdrawlforsamepurpose ? (
          <input
            type="number"
            value={editData[selectedId]?.priorwithdrawlamount || ""}
            onChange={(e) =>
              handleEditChange(
                selectedId,
                "priorwithdrawlamount",
                e.target.value
              )
            }
          />
        ) : (
          <b>₹{details?.priorwithdrawlamount}</b>
        )}
      </div>

      {/* Year (conditional) */}
      <div className="detail-item">
        <span>Year</span>

        {isRowEditable &&
        editData[selectedId]?.ispriorwithdrawlforsamepurpose ? (
          <input
            value={editData[selectedId]?.priorwithdrawlfinyear || ""}
            onChange={(e) =>
              handleEditChange(
                selectedId,
                "priorwithdrawlfinyear",
                e.target.value
              )
            }
          />
        ) : (
          <b>{details?.priorwithdrawlfinyear}</b>
        )}
      </div>

    </div>

  </div>
)}

        {/* ================= ADVANCE ================= */}
        {appType === "advance" && (
          <div className="section-block">
            <h4>Advance Request</h4>
            <div className="details-grid details-grid-4">
              <div className="detail-item critical"><span>Requested</span>{isRowEditable ? (
  <input
    value={editData[selectedId]?.amountofadvancerequested || ""}
    onChange={(e) =>
      handleEditChange(
        selectedId,
        "amountofadvancerequested",
        e.target.value
      )
    }
  />
) : (
  <b>₹{details?.amountofadvancerequested}</b>
)}</div>
              <div className="detail-item"><span>Purpose</span>{isRowEditable ? (
  <input
    value={editData[selectedId]?.purposeofadvance || ""}
    onChange={(e) =>
      handleEditChange(selectedId, "purposeofadvance", e.target.value)
    }
  />
) : (
  <b>{details?.purposeofadvance}</b>
)}</div>
              <div className="detail-item"><span>Particulars</span><b>{details?.particulars}</b></div>
              <div className="detail-item critical"><span>Rule</span>{isRowEditable ? (
  <select
    value={editData[selectedId]?.advancerule || ""}
    onChange={(e) =>
      handleEditChange(
        selectedId,
        "advancerule",
        Number(e.target.value)
      )
    }
  >
    <option value="">-- Select Rule --</option>

    {advanceRules.map(rule => (
      <option key={rule.ruleId} value={rule.ruleId}>
        {rule.ruleDescription}
      </option>
    ))}
  </select>
) : (
  <b>{details?.advanceruleText}</b>
)}</div>
            </div>

            <RuleDetailsSection
  appId={selectedId}
  ruleId={
    editData[selectedId]?.advancerule ??
    details?.advancerule
  }
  jsonData={
    isRowEditable
      ? editData[selectedId]?.ruleSpecificDataJson
      : details?.ruleSpecificDataJson
  }
  isRowEditable={isRowEditable}
  editData={editData}
  handleEditChange={handleRuleFieldChange}
/>

            <div className="details-grid details-grid-4">
              <div className="detail-item"><span>Installments</span><b>{details?.noofmonthlyinstallmentsforpaymentofconsolidatedadvance}</b></div>
              <div className="detail-item"><span>Outstanding</span><b>₹{details?.amountofadvanceoutstanding}</b></div>
              <div className="detail-item"><span>Consolidated</span><b>₹{details?.amountofconsolidatedadvance}</b></div>
            </div>
          </div>
        )}


{isRowEditable && (
  <div style={{ marginTop: "15px", textAlign: "right" }}>
    <button
      className="process-btn"
      onClick={() => handleUpdate(selectedId)}
    >
      Save Changes
    </button>
  </div>
)}
      </div>

      {/* RIGHT PANEL */}
  

    </div>
  )}
</div>
{/* ================= ACTION BAR ================= */}


</div>
<div className="workflow-action-bar">
<div className="action-bar-inner">
  <select
    className="action-dropdown"
    value={selectedAction}
    onChange={(e) => setSelectedAction(e.target.value)}
  >
    <option value="">-- Select Action --</option>
    {actions.map(action => (
      <option key={action.actionId} value={action.actionId}>
        {action.actionDesc}
      </option>
    ))}
  </select>

  <input
    type="text"
    className="remarks-input"
    placeholder="Enter remarks..."
    value={remarks}
    onChange={(e) => setRemarks(e.target.value)}
  />

  {/* SEND TO */}
  {(sendToOptions.length > 0 || selectedDetails?.isReturned) && (
    <select
      className="action-dropdown"
      value={selectedSendTo}
      onChange={(e) => setSelectedSendTo(e.target.value)}
      disabled={selectedDetails?.isReturned && sendToOptions.length === 1}
    >
      <option value="">-- Send To --</option>

      {sendToOptions.map(role => (
        <option key={role.roleId} value={role.roleId}>
          {role.roleName}
        </option>
      ))}
    </select>
  )}

  <button
    className="process-btn"
    onClick={handleProcess}
  >
    {getButtonText()}
  </button>

</div>
</div>
</div>
);
};

export default GpfWorkflowPage;
