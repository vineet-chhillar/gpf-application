import React, { useState, useEffect } from "react";
import api from "../api/axios";
import "../styles/GpfWorkflowPage.css";

//const GpfWorkflowPage = ({ roleId, roleName }) => {
  const GpfWorkflowPage = () => {

  const [applications, setApplications] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);
  const [actions, setActions] = useState([]);
  const [selectedAction, setSelectedAction] = useState("");
  const [remarks, setRemarks] = useState("");
const [expandedRow, setExpandedRow] = useState(null);
const [trailMap, setTrailMap] = useState({});
const [detailsMap, setDetailsMap] = useState({});


const toggleExpand = async (applicationId, empCode) => {

  if (expandedRow === applicationId) {
    setExpandedRow(null);
    return;
  }

  try {

    const [trailRes, detailsRes] = await Promise.all([
      api.get(`/gpf-withdrawl/trail/${applicationId}`),
      api.get(`/gpf-withdrawl/status/${empCode}`)
    ]);

    setTrailMap(prev => ({
      ...prev,
      [applicationId]: trailRes.data
    }));

    setDetailsMap(prev => ({
      ...prev,
      [applicationId]: detailsRes.data
    }));

  } catch (err) {
    console.error("Expand load error", err);
  }

  setExpandedRow(applicationId);
};



  /* ================= LOAD INBOX ================= */
  {/*useEffect(() => {
    if (!roleId) return;
    api.get(`/gpf-withdrawl/inbox/${roleId}`)
      .then(res => setApplications(res.data))
      .catch(err => console.error("Inbox load error", err));
  }, [roleId]);*/}
useEffect(() => {

  api.get(`/gpf-withdrawl/inbox`)
    .then(res => {
      setApplications(res.data);
    })
    .catch(err => console.error("Inbox load error", err));

}, []);
  /* ================= LOAD ACTIONS ================= */
  useEffect(() => {
  api.get("/gpf-withdrawl/actions")
    .then(res => {
      console.log("Actions response:", res.data);
      setActions(Array.isArray(res.data) ? res.data : []);

    })
    .catch(err => console.error("Action load error", err));
}, []);


  /* ================= CHECKBOX ================= */
  const toggleSelection = (id) => {
    setSelectedIds(prev =>
      prev.includes(id)
        ? prev.filter(x => x !== id)
        : [...prev, id]
    );
  };

  const toggleSelectAll = () => {
    if (selectedIds.length === applications.length) {
      setSelectedIds([]);
    } else {
      setSelectedIds(applications.map(app => app.applicationId));
    }
  };

  /* ================= PROCESS ================= */
 const handleProcess = async () => {

  if (selectedIds.length === 0) {
    alert("Select at least one application");
    return;
  }

  if (!selectedAction) {
    alert("Select action");
    return;
  }

  try {

    await api.post("/gpf-withdrawl/process", {
      applicationIds: selectedIds,
      actionId: selectedAction,
      remarks
    });

    alert("Processed successfully");

    const res = await api.get(`/gpf-withdrawl/inbox`);
    setApplications(res.data);

    setSelectedIds([]);
    setSelectedAction("");
    setRemarks("");

  } catch (err) {
    alert(err.response?.data || "Processing failed");
  }
};
{/*useEffect(() => {
  console.log("ROLE ID:", roleId);
}, [roleId]);*/}

  return (
    
    <div className="workflow-container">

      {/*<h2>{roleName} Inbox</h2>*/}
      <h2>Pending Applications</h2>

      {/* ================= INBOX TABLE ================= */}
      <div className="workflow-table-wrapper">

        <table className="workflow-table">
          <thead>
<tr>
<th className="col-check">
<input
type="checkbox"
onChange={toggleSelectAll}
checked={
applications.length > 0 &&
selectedIds.length === applications.length
}
/>
</th>

<th className="col-id">App ID</th>
<th className="col-empcode">Emp Code</th>
<th className="col-name">Name</th>
<th className="col-designation">Designation</th>
<th className="col-amount">Amount</th>
<th className="col-date">Date</th>
<th className="col-purpose">Purpose</th>
<th className="col-pending">Pending With</th>
</tr>
</thead>

          <tbody>
  {applications.map(app => (
  <React.Fragment key={app.applicationId}>

      {/* MAIN ROW */}
      <tr
  className={`main-row ${
    expandedRow === app.applicationId ? "selected-row" : ""
  }`}
  onClick={() => toggleExpand(app.applicationId, app.empCode)}
>
        <td>
          {app.pendingWithRole !== "Completed" && (
          <input
            type="checkbox"
            checked={selectedIds.includes(app.applicationId)}
            onChange={(e) => {
              e.stopPropagation();
              toggleSelection(app.applicationId)
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

<td className="purpose-cell">
{app.purpose}
</td>

<td className="pending-role">
{app.pendingWithRole}
</td>
      </tr>

      {/* EXPANDED ROW */}
     {expandedRow === app.applicationId && (() => {

  const full = detailsMap[app.applicationId] || {};
  const master = full.master || {};
  const details = full.details || {};

  return (
    <tr className="expanded-row">
      <td colSpan="9">
        <div className="expanded-content">

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
<span className="label">Outstanding Balance Date</span>
<span className="value">{details.dateofoutstandingbalance}</span>
</div>

<div className="detail-item">
<span className="label">Outstanding Balance</span>
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

            <h4>Withdrawal Calculation</h4>

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

            <h4>Withdrawal Request</h4>

<div className="details-grid">

<div className="detail-item">
<span className="label">Requested Amount</span>
<span className="value">₹{details.amountofwithdrawlrequested}</span>
</div>

<div className="detail-item">
<span className="label">Purpose</span>
<span className="value">{details.purposeofwithdrawl}</span>
</div>

<div className="detail-item">
<span className="label">Withdrawal Rule</span>
<span className="value">{details.withdrawlrule}</span>
</div>

<div className="detail-item">
<span className="label">Application Date</span>
<span className="value">
{details.dateofapplication
? new Date(details.dateofapplication).toLocaleDateString()
: ""}
</span>
</div>

</div>

            <h4>Previous Withdrawal Details</h4>

<div className="details-grid">

<div className="detail-item">
<span className="label">Prior Withdrawal</span>
<span className="value">
{details.ispriorwithdrawlforsamepurpose ? "Yes" : "No"}
</span>
</div>

<div className="detail-item">
<span className="label">Previous Amount</span>
<span className="value">₹{details.priorwithdrawlamount}</span>
</div>

<div className="detail-item">
<span className="label">Financial Year</span>
<span className="value">{details.priorwithdrawlfinyear}</span>
</div>

</div>

          </div>

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
      </td>
    </tr>
  );

})()}


    </React.Fragment>
  ))}
</tbody>


        </table>
      </div>

      {/* ================= ACTION PANEL ================= */}
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

  <button
    className="process-btn"
    onClick={handleProcess}
  >
    Process Selected
  </button>

</div>

    </div>
    
  );
};

export default GpfWorkflowPage;
