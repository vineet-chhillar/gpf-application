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
              <th>
                <input
                  type="checkbox"
                  onChange={toggleSelectAll}
                  checked={
                    applications.length > 0 &&
                    selectedIds.length === applications.length
                  }
                />
              </th>
              <th>Application ID</th>
              <th>Employee</th>
              <th>Amount</th>
              <th>Date</th>
              <th>Pending With</th>
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
<td>{app.employeeName}</td>
<td>{app.amount}</td>
<td>
  {app.applicationDate
    ? new Date(app.applicationDate).toLocaleDateString()
    : ""}
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
      <td colSpan="6">
        <div className="expanded-content">

          <div className="left-panel">

            <h4>Employee Details</h4>

            <p><strong>Employee Code:</strong> {master.empcode}</p>
            <p><strong>Name:</strong> {master.empname}</p>
            <p><strong>Designation:</strong> {master.designation}</p>
            <p><strong>Division:</strong> {master.empdivision}</p>
            <p><strong>Mobile:</strong> {master.empmobileno}</p>
            <p><strong>Email:</strong> {master.empemailid}</p>
            <p><strong>Date of Joining:</strong> {master.dateofjoining}</p>
            <p><strong>Retirement Date:</strong> {master.dateofsuperannuation}</p>

            <hr />

            <h4>GPF Account Summary</h4>

            <p><strong>GPF Account No:</strong> {details.gpfaccountno}</p>
            <p><strong>Basic Pay:</strong> ₹{details.basicpay}</p>

            <p><strong>Outstanding Balance Date:</strong> {details.dateofoutstandingbalance}</p>
            <p><strong>Outstanding Balance:</strong> ₹{details.outstandingbalance}</p>

            <p><strong>Credit From:</strong> {details.creditfromdate}</p>
            <p><strong>Credit To:</strong> {details.credittodate}</p>

            <p><strong>Total Credit Amount:</strong> ₹{details.totalcreditamount}</p>
            <p><strong>Refund After Balance Date:</strong> ₹{details.refundafterdateofoutstandingbalance}</p>

            <hr />

            <h4>Withdrawal Calculation</h4>

            <p><strong>Withdraw From:</strong> {details.withdrawlfromdate}</p>
            <p><strong>Withdraw To:</strong> {details.withdrawltodate}</p>

            <p><strong>Total Withdrawal Amount:</strong> ₹{details.totalwithdrawlamount}</p>
            <p><strong>Net Balance:</strong> ₹{details.netbalance}</p>

            <hr />

            <h4>Withdrawal Request</h4>

            <p><strong>Requested Amount:</strong> ₹{details.amountofwithdrawlrequested}</p>
            <p><strong>Purpose:</strong> {details.purposeofwithdrawl}</p>
            <p><strong>Withdrawal Rule:</strong> {details.withdrawlrule}</p>

            <p>
              <strong>Application Date:</strong>{" "}
              {details.dateofapplication
                ? new Date(details.dateofapplication).toLocaleDateString()
                : ""}
            </p>

            <hr />

            <h4>Previous Withdrawal Details</h4>

            <p>
              <strong>Prior Withdrawal For Same Purpose:</strong>{" "}
              {details.ispriorwithdrawlforsamepurpose ? "Yes" : "No"}
            </p>

            <p><strong>Previous Withdrawal Amount:</strong> ₹{details.priorwithdrawlamount}</p>

            <p><strong>Previous Financial Year:</strong> {details.priorwithdrawlfinyear}</p>

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
