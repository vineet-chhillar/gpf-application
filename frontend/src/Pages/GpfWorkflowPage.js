import React, { useState, useEffect } from "react";
import api from "../api/axios";
import "../styles/GpfWorkflowPage.css";
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

setExpandedRow(applicationId);
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

  const selectedApp = applications.find(
    a => Number(a.applicationId) === Number(selectedId)
  );

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
useEffect(() => {
  if (!selectedId) return;

  const app = applications.find(
    a => Number(a.applicationId) === Number(selectedId)
  );

  if (app) {
    toggleExpand(app.applicationId, app.empCode);
  }
}, [selectedId, applications]); // 🔥 add applications
console.log("Applications:", applications);
console.log("Looking for ID:", selectedId);
useEffect(() => {

if (!selectedId) {
  {/*setSendToOptions([]);*/}
   setSendToOptions([]);
  setSelectedSendTo("");
  return;
}

const selectedApp = applications.find(
  a => Number(a.applicationId) === Number(selectedId)
);

if (!selectedApp) return;

console.log("Selected App:", selectedApp);

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
      `${base}/status/${selectedApp.empCode}`
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

  // 🔁 If send back selected
  const isReturnMode =
  selectedDetails?.isReturned && selectedDetails?.returnFromStep != null;

  if (selectedSendTo || isReturnMode)  {

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
// ================= RULE HELPERS =================






return (

<div className="workflow-container">

{/* TYPE SELECTOR */}

<div className="type-selector">

<label className={appType === "withdrawl" ? "active" : ""}>
<input
type="radio"
value="withdrawl"
checked={appType === "withdrawl"}
onChange={() => setAppType("withdrawl")}
/>
<span>Withdrawal</span>
</label>

<label className={appType === "advance" ? "active" : ""}>
<input
type="radio"
value="advance"
checked={appType === "advance"}
onChange={() => setAppType("advance")}
/>
<span>Advance</span>
</label>

</div>

<h2>
{appType === "withdrawl"
? "Withdrawal Applications"
: "Advance Applications"}
</h2>

{/* ================= TABLE ================= */}

<div className="workflow-table-wrapper">

<table className="workflow-table">

<thead>

<tr>
<th>

</th>

<th>App ID</th>
<th>Emp Code</th>
<th>Name</th>
<th>Designation</th>
<th>Amount</th>
<th>Date</th>
<th>Purpose</th>
<th>Pending With</th>
</tr>

</thead>

<tbody>

{applications.map(app => (

<React.Fragment key={app.applicationId}>

<tr
className={`main-row ${expandedRow === app.applicationId ? "selected-row" : ""}`}
onClick={() => toggleExpand(app.applicationId, app.empCode)}
>

<td>
{app.pendingWithRole !== "Completed" && (
<input
type="radio"
name="applicationSelect"
checked={selectedId === app.applicationId}
onChange={(e) => {
e.stopPropagation();
handleSelection(app.applicationId)
}}
/>
)}
</td>

<td>{app.applicationId}</td>
<td>{app.empCode}</td>
<td>{app.employeeName}</td>
<td>{app.designation}</td>
<td>₹{app.amount}</td>

<td>
{app.applicationDate
? new Date(app.applicationDate).toLocaleDateString()
: ""}
</td>

<td>{app.purpose}</td>
<td>{app.pendingWithRole}</td>

</tr>

{/* ================= EXPANDED ROW ================= */}

{expandedRow === app.applicationId && (

(() => {
  const full = detailsMap[app.applicationId] || {};
  const master = full.master || {};
  const details = full.details || {};
  const isRowEditable = details?.isReturned === true;

  const jsonData = isRowEditable
    ? editData[app.applicationId]?.ruleSpecificDataJson
    : details.ruleSpecificDataJson;

  const ruleId =
    editData[app.applicationId]?.advancerule ??
    details.advancerule;

  return (

<tr className="expanded-row">
<td colSpan="9">

<div className="expanded-content">

{/* LEFT PANEL */}

<div className="left-panel">

<h4>Employee Details</h4>

<div className="details-grid">

<div className="detail-item">
<span className="label">Employee Code</span>
<span className="value">{master.empcode}</span>
</div>

<div className="detail-item">
<span className="label">Name</span>
<span className="value">{master.empname}</span>
</div>

<div className="detail-item">
<span className="label">Designation</span>
<span className="value">{master.designation}</span>
</div>

<div className="detail-item">
<span className="label">Division</span>
<span className="value">{master.empdivision}</span>
</div>

<div className="detail-item">
<span className="label">Mobile</span>
<span className="value">{master.empmobileno}</span>
</div>

<div className="detail-item">
<span className="label">Email</span>
<span className="value">{master.empemailid}</span>
</div>

<div className="detail-item">
<span className="label">Joining Date</span>
<span className="value">{master.dateofjoining}</span>
</div>

<div className="detail-item">
<span className="label">Retirement</span>
<span className="value">{master.dateofsuperannuation}</span>
</div>

</div>


<h4>GPF Account Summary</h4>

<div className="details-grid">

<div className="detail-item">
<span className="label">GPF Account No</span>
<span className="value">{details.gpfaccountno}</span>
</div>

<div className="detail-item">
<span className="label">Basic Pay</span>
<span className="value">₹{details.basicpay}</span>
</div>

<div className="detail-item">
<span className="label">Closing Balance Date</span>
<span className="value">{details.dateofoutstandingbalance}</span>
</div>

<div className="detail-item">
<span className="label">Closing Balance</span>
<span className="value">₹{details.outstandingbalance}</span>
</div>

<div className="detail-item">
<span className="label">Credit From</span>
<span className="value">{details.creditfromdate}</span>
</div>

<div className="detail-item">
<span className="label">Credit To</span>
<span className="value">{details.credittodate}</span>
</div>

<div className="detail-item">
<span className="label">Total Credit Amount</span>
<span className="value">₹{details.totalcreditamount}</span>
</div>

<div className="detail-item">
<span className="label">Refund After Balance</span>
<span className="value">₹{details.refundafterdateofoutstandingbalance}</span>
</div>

</div>


{/* COMMON CALCULATION */}

<h4>Balance Calculation</h4>

<div className="details-grid">

<div className="detail-item">
<span className="label">Withdraw From</span>
<span className="value">{details.withdrawlfromdate}</span>
</div>

<div className="detail-item">
<span className="label">Withdraw To</span>
<span className="value">{details.withdrawltodate}</span>
</div>

<div className="detail-item">
<span className="label">Total Withdrawal</span>
<span className="value">₹{details.totalwithdrawlamount}</span>
</div>

<div className="detail-item">
<span className="label">Net Balance</span>
<span className="value">₹{details.netbalance}</span>
</div>

</div>


{/* WITHDRAWAL SECTION */}

{appType === "withdrawl" && (

<>

<h4>Withdrawal Request</h4>

<div className="details-grid">

<div className="detail-item">
<span className="label">Requested Amount</span>
{isRowEditable ? (
  <input
    value={editData[app.applicationId]?.amountofwithdrawlrequested || ""}
    onChange={(e) =>
      handleEditChange(app.applicationId, "amountofwithdrawlrequested", e.target.value)
    }
  />
) : (
  <span className="value">₹{details.amountofwithdrawlrequested}</span>
)}
</div>

<div className="detail-item">
<span className="label">Purpose</span>
{isRowEditable ? (
  <input
    value={editData[app.applicationId]?.purposeofwithdrawl || ""}
    onChange={(e) =>
      handleEditChange(app.applicationId, "purposeofwithdrawl", e.target.value)
    }
  />
) : (
  <span className="value">{details.purposeofwithdrawl}</span>
)}
</div>

<div className="detail-item">
  <span className="label">Withdrawal Rule</span>

  {isRowEditable ? (
    <select
      value={
        editData[app.applicationId]?.withdrawlrule ??
        details.withdrawlrule ??
        ""
      }
      onChange={(e) =>
        handleEditChange(
          app.applicationId,
          "withdrawlrule",
          Number(e.target.value)
        )
      }
    >
      <option value="">-- Select Rule --</option>

      {withdrawlRules.map(rule => (
        <option
          key={rule.ruleId}
          value={rule.ruleId}
        >
          {rule.withdrawlReason}
        </option>
      ))}
    </select>

  ) : (
    <span className="value">{details.withdrawlruleText}</span>
  )}
</div>

<div className="detail-item">
  <span className="label">Concerned Officer</span>
  {isRowEditable ? (
  <input
    value={editData[app.applicationId]?.concernedofficername || ""}
    onChange={(e) =>
      handleEditChange(app.applicationId, "concernedofficername", e.target.value)
    }
  />
) : (
  <span className="value">{details.concernedofficername}</span>
)}
</div>

<div className="detail-item">
<span className="label">Application Date</span>
<span className="value">{details.dateofapplication}</span>
</div>

</div>


<h4>Previous Withdrawal Details</h4>

<div className="details-grid">

<div className="detail-item">
<span className="label">Prior Withdrawal</span>
{isRowEditable ? (
  <input
    type="checkbox"
    checked={editData[app.applicationId]?.ispriorwithdrawlforsamepurpose || false}
    onChange={(e) =>
      handleEditChange(app.applicationId, "ispriorwithdrawlforsamepurpose", e.target.checked)
    }
  />
) : (
  <span className="value">
    {details.ispriorwithdrawlforsamepurpose ? "Yes" : "No"}
  </span>
)}
</div>

<div className="detail-item">

  <span className="label">Previous Amount</span>

  {isRowEditable &&
 editData[app.applicationId]?.ispriorwithdrawlforsamepurpose ? (
    <input
      type="number"
      value={editData[app.applicationId]?.priorwithdrawlamount || ""}
      onChange={(e) =>
        handleEditChange(
          app.applicationId,
          "priorwithdrawlamount",
          e.target.value
        )
      }
    />
  ) : (
    <span className="value">₹{details.priorwithdrawlamount}</span>
  )}

</div>

<div className="detail-item">

  <span className="label">Financial Year</span>

  {isRowEditable &&
 editData[app.applicationId]?.ispriorwithdrawlforsamepurpose ? (
    <input
      value={editData[app.applicationId]?.priorwithdrawlfinyear || ""}
      onChange={(e) =>
        handleEditChange(
          app.applicationId,
          "priorwithdrawlfinyear",
          e.target.value
        )
      }
      placeholder="e.g. 2023-24"
    />
  ) : (
    <span className="value">{details.priorwithdrawlfinyear}</span>
  )}
</div>


</div>

</>

)}


{/* ADVANCE SECTION */}

{appType === "advance" && (

<>

<h4>Advance Request</h4>

<div className="details-grid">

<div className="detail-item">
<span className="label">Advance Requested</span>
{isRowEditable ? (
<input
  value={editData[app.applicationId]?.amountofadvancerequested || ""}
  onChange={(e) =>
    handleEditChange(app.applicationId, "amountofadvancerequested", e.target.value)
  }
/>) : (
  <span className="value">₹{details.amountofadvancerequested}</span>
)}
</div>

<div className="detail-item">
<span className="label">Purpose</span>

{isRowEditable ? (
  <input
    value={editData[app.applicationId]?.purposeofadvance || ""}
    onChange={(e) =>
      handleEditChange(app.applicationId, "purposeofadvance", e.target.value)
    }
  />
) : (
  <span className="value">{details.purposeofadvance}</span>
)}
</div>



<div className="detail-item">
  <span className="label">Particulars</span>
    {isRowEditable ? (
  <input
    value={editData[app.applicationId]?.particulars || ""}
    onChange={(e) =>
      handleEditChange(app.applicationId, "particulars", e.target.value)
    }
  />
) : (
  <span className="value">{details.particulars}</span>
)}
</div>

<div className="detail-item">
  <span className="label">Advance Rule</span>

  {isRowEditable ? (
    <select
      value={editData[app.applicationId]?.advancerule || ""}
      onChange={(e) =>
        handleEditChange(
          app.applicationId,
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
    <span className="value">
      {details.advanceruleText || details.ruleName || "-"}
    </span>
  )}

</div>
</div>


{/* ================= RULE SPECIFIC DETAILS ================= */}



<RuleDetailsSection
  appId={app.applicationId}
  ruleId={
    editData[app.applicationId]?.advancerule ??
    details.advancerule
  }
  jsonData={
    isRowEditable
      ? editData[app.applicationId]?.ruleSpecificDataJson
      : details.ruleSpecificDataJson
  }
  isRowEditable={isRowEditable}
  editData={editData}
  handleEditChange={handleRuleFieldChange}   // 🔥 IMPORTANT (use fixed handler)
/>

<div className="detail-grid">
<div className="detail-item">
<span className="label">Installments</span>
{isRowEditable ? (
  <input
    value={editData[app.applicationId]?.noofmonthlyinstallmentsforpaymentofconsolidatedadvance || ""}
    onChange={(e) =>
      handleEditChange(app.applicationId, "noofmonthlyinstallmentsforpaymentofconsolidatedadvance", e.target.value)
    }
  />
) : (
  <span className="value">
    {details.noofmonthlyinstallmentsforpaymentofconsolidatedadvance}
  </span>
)}
</div>

<div className="detail-item">
<span className="label">Advance Outstanding</span>
<span className="value">₹{details.amountofadvanceoutstanding}</span>
</div>

<div className="detail-item">
  <span className="label">Consolidated Advance</span>

  <span className="value">
    ₹{
      editData[app.applicationId]?.amountofconsolidatedadvance ??
      details.amountofconsolidatedadvance ??
      0
    }
  </span>
</div>

</div>



</>

)}

</div>


{/* RIGHT PANEL */}

<div className="right-panel">

<h4>Status Trail</h4>

{trailMap[app.applicationId]?.length > 0 ? (
trailMap[app.applicationId].map((t, i) => (
<div key={i} className="trail-box">
<div><strong>Role:</strong> {t.role}</div>
<div><strong>Action:</strong> {t.action}</div>
<div><strong>Date:</strong> {t.time || t.actionAt}</div>
<div className="remarks">{t.remarks}</div>
</div>
))
) : (
<p>No trail available</p>
)}

</div>

</div>
{isRowEditable && (
  <div style={{ marginTop: "15px", textAlign: "right" }}>
    <button
      className="process-btn"
      onClick={() => handleUpdate(app.applicationId)}
    >
      Save Changes
    </button>
  </div>
)}
</td>
</tr>

);

})()
)}

</React.Fragment>

))}

</tbody>

</table>

</div>

{/* ================= ACTION BAR ================= */}

<div className="workflow-action-bar">

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
{/* 🔥 NEW SEND TO DROPDOWN */}

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

);

};

export default GpfWorkflowPage;