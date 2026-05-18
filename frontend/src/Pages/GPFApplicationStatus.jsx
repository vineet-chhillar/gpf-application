import React, { useEffect, useState } from "react";
import "../styles/WithdrawlApplicationStatus.css";
import api from "../api/axios";
/* temporary until login integration */
const HARDCODED_EMPCODE = "EMP021";

function WithdrawlApplicationStatus() {

  const [expandedRow, setExpandedRow] = useState(null);
const [search, setSearch] = useState("");
const [appType, setAppType] = useState("withdrawl");

const [roleFilter, setRoleFilter] = useState("");
  {/*const [application, setApplication] = useState({
    master: {},
    details: {},
    trail: []
  });*/}
const formatLabel = (key) => {
  return key
    .replace(/([A-Z])/g, " $1")  // camelCase → spaced
    .replace(/^./, str => str.toUpperCase()); // capitalize
};
const [applications, setApplications] = useState([]);
const filteredApps = applications.filter(a =>
  (!roleFilter || a.currentOwnerRole === roleFilter) &&

  (
    a.master?.empname?.toLowerCase().includes(search.toLowerCase()) ||
    a.master?.empcode?.toLowerCase().includes(search.toLowerCase())
  )
);
  const [loading, setLoading] = useState(true);

 useEffect(() => {

const loadData = async () => {
try {

const base =
appType === "withdrawl"
? "/gpf-withdrawl"
: "/gpf-advance";

const res = await api.get(`${base}/status-all`);

setApplications(res.data);
setExpandedRow(null);

} catch (err) {
console.error("Failed to load workflow status", err);
} finally {
setLoading(false);
}
};

loadData();

}, [appType]);

  const toggleRow = (index) => {
    setExpandedRow(expandedRow === index ? null : index);
  };

  if (loading) {
    return <div className="status-container">Loading...</div>;
  }
const pendingApps = applications.filter(
  a =>
    a.currentOwnerRole !== "Completed" &&
    a.currentOwnerRole !== "Cancelled/Rejected"
);

const pendingByRole = applications
  .filter(
    a =>
      a.currentOwnerRole !== "Completed" &&
      a.currentOwnerRole !== "Cancelled/Rejected"
  )
  .reduce((acc, app) => {
    const role = app.currentOwnerRole || "Unknown";

    if (!acc[role]) {
      acc[role] = 0;
    }

    acc[role]++;
    return acc;
  }, {});
  //const { master, details, trail } = application;

  return (
    <div className="status-container">
<div className="top-bar">

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


 <div className="dashboard-cards">

  <div className="card total">
    <h3>Total Applications</h3>
    <p>{applications.length}</p>
  </div>

  <div className="card pending">
    <h3>Pending</h3>
    <p>
      {
        applications.filter(
          a =>
            a.currentOwnerRole !== "Completed" &&
            a.currentOwnerRole !== "Cancelled/Rejected"
        ).length
      }
    </p>
  </div>
  {/*<div className="card pending-role-card">
  <h3>Pending by Role</h3>

  {Object.keys(pendingByRole).length === 0 ? (
    <p>No pending</p>
  ) : (
    <div className="role-list">
      {Object.entries(pendingByRole).map(([role, count]) => (
        <div key={role} className="role-item">
          <span>{role}</span>
          <b>{count}</b>
        </div>
      ))}
    </div>
  )}
</div>*/}

  <div className="card completed">
    <h3>Completed</h3>
    <p>
      {
        applications.filter(
          a => a.currentOwnerRole === "Completed"
        ).length
      }
    </p>
  </div>

  {/* 🔥 NEW CARD */}
  <div className="card rejected">
    <h3>Cancelled / Rejected</h3>
    <p>
      {
        applications.filter(
          a => a.currentOwnerRole === "Cancelled/Rejected"
        ).length
      }
    </p>
  </div>

</div>


 <div className="search-container">
    <input
      type="text"
      className="search-input"
      placeholder="Search..."
      value={search}
      onChange={(e) => setSearch(e.target.value)}
    />
  </div>
  
</div>
<div className="pending-role-row">
  
  <div className="pending-role-header">

  <span className="pending-role-title">
    Pending by Role →
  </span>

  {roleFilter && (
    <span
      className="clear-filter"
      onClick={() => setRoleFilter("")}
    >
      ✖
    </span>
  )}

</div>

  <div className="pending-role-inline">
    {Object.entries(pendingByRole).map(([role, count], index) => (
      <span
  key={role}
  className="role-inline-item"
  onClick={() => setRoleFilter(role)}
>
        <span className="role-name">{role}:</span>
        <span className="role-count">{count}</span>

        {index !== Object.entries(pendingByRole).length - 1 && (
          <span className="role-separator">|</span>
        )}
      </span>
    ))}
  </div>

</div>
      <div className="status-table-wrapper">
        <table className="status-table">

         <thead>
<tr>
  <th>Employee Code</th>
  <th>Name</th>
  <th>Designation</th>
  <th>Division</th>
  <th>Basic Pay</th>
  <th>Net Balance</th>
 <th>Status</th>
<th>Pending With</th>
<th>Last Action By</th>
<th>Last Remarks</th>
</tr>
</thead>

          <tbody>

{filteredApps.map((app, index) => {

const master = app.master || {};
const details = app.details || {};
const trail = app.trail || [];
const lastTrail = trail.length > 0 ? trail[trail.length - 1] : {};
return (
<React.Fragment key={index}>

<tr
  className={`main-row ${expandedRow === index ? "status-selected-row" : ""}`}
  onClick={() => toggleRow(index)}
>
  <td>{master.empcode}</td>
  <td>{master.empname}</td>
  <td>{master.designation}</td>
  <td>{master.empdivision}</td>
  <td>{details.basicpay}</td>
  <td>{details.netbalance}</td>

<td>
  {(() => {
    const isRejected =
      app.currentOwnerRole === "Cancelled/Rejected";

    const isCompleted =
      app.currentOwnerRole === "Completed";

    return (
      <span
        className={
          isRejected
            ? "status-reject"
            : isCompleted
            ? "status-complete"
            : "status-pending"
        }
      >
        {app.currentOwnerRole}
      </span>
    );
  })()}
</td>

<td><span className="pending-role">{app.currentOwnerRole}</span></td>
<td>{app.lastActionByRole}</td>
<td className="remarks-column">{app.lastRemarks}</td>
</tr>

{expandedRow === index && (
<tr className="expanded-row">
<td colSpan="10">

<div className="status-expanded-content">

{/* LEFT PANEL */}
<div className="status-left-panel">

<h4>Employee Information</h4>

<div className="details-grid">

<div className="status-detail-item">
<div className="detail-label">Mobile</div>
<div className="detail-value">{master.empmobileno}</div>
</div>

<div className="status-detail-item">
<div className="detail-label">Email</div>
<div className="detail-value">{master.empemailid}</div>
</div>

<div className="status-detail-item">
<div className="detail-label">Date of Joining</div>
<div className="detail-value">{master.dateofjoining}</div>
</div>

<div className="status-detail-item">
<div className="detail-label">Retirement</div>
<div className="detail-value">{master.dateofsuperannuation}</div>
</div>

</div>



<h4>GPF Account Summary</h4>

<div className="details-grid">

<div className="status-detail-item">
<div className="detail-label">Basic Pay</div>
<div className="detail-value">₹{details.basicpay}</div>
</div>

<div className="status-detail-item">
<div className="detail-label">Outstanding Balance Date</div>
<div className="detail-value">{details.dateofoutstandingbalance}</div>
</div>

<div className="status-detail-item">
<div className="detail-label">Outstanding Balance</div>
<div className="detail-value">₹{details.outstandingbalance}</div>
</div>

<div className="status-detail-item">
<div className="detail-label">Credit From</div>
<div className="detail-value">{details.creditfromdate}</div>
</div>

<div className="status-detail-item">
<div className="detail-label">Credit To</div>
<div className="detail-value">{details.credittodate}</div>
</div>

<div className="status-detail-item">
<div className="detail-label">Total Credit Amount</div>
<div className="detail-value">₹{details.totalcreditamount}</div>
</div>

<div className="status-detail-item">
<div className="detail-label">Refund After Balance Date</div>
<div className="detail-value">₹{details.refundafterdateofoutstandingbalance}</div>
</div>

</div>


<h4>Withdrawal Calculation</h4>

<div className="details-grid">

<div className="status-detail-item">
<div className="detail-label">Withdraw From</div>
<div className="detail-value">{details.withdrawlfromdate}</div>
</div>

<div className="status-detail-item">
<div className="detail-label">Withdraw To</div>
<div className="detail-value">{details.withdrawltodate}</div>
</div>

<div className="status-detail-item">
<div className="detail-label">Total Withdrawal Amount</div>
<div className="detail-value">₹{details.totalwithdrawlamount}</div>
</div>

<div className="status-detail-item highlight">
<div className="detail-label">Net Balance</div>
<div className="detail-value">₹{details.netbalance}</div>
</div>

</div>



{appType === "withdrawl" ? (

<>

<h4>Withdrawal Request</h4>

<div className="details-grid">

<div className="status-detail-item highlight">
<div className="detail-label">Requested Amount</div>
<div className="detail-value">₹{details.amountofwithdrawlrequested}</div>
</div>

<div className="status-detail-item detail-wide">
<div className="detail-label">Purpose</div>
<div className="detail-value">{details.purposeofwithdrawl}</div>
</div>

<div className="status-detail-item highlight">
<div className="detail-label">Withdrawal Rule</div>
<div className="detail-value">{details.withdrawlruleText}</div>
</div>

<div className="status-detail-item">
  <span className="label">Concerned Officer</span>
  <span className="value">{details.concernedofficername}</span>
</div>

<div className="status-detail-item">
<div className="detail-label">Application Date</div>
<div className="detail-value">
{details.dateofapplication
? new Date(details.dateofapplication).toLocaleDateString()
: ""}
</div>
</div>

</div>

</>

) : (

<>

<h4>Advance Request</h4>

<div className="details-grid">

<div className="status-detail-item highlight">
<div className="detail-label">Advance Requested</div>
<div className="detail-value">₹{details.amountofadvancerequested}</div>
</div>

<div className="status-detail-item detail-wide">
<div className="detail-label">Purpose</div>
<div className="detail-value">{details.purposeofadvance}</div>
</div>

<div className="status-detail-item detail-wide">
  <div className="detail-label">Particulars</div>
  <div className="detail-value">{details.particulars}</div>
</div>

<div className="status-detail-item highlight">
<div className="detail-label">Advance Rule</div>
<div className="detail-value">{details.advanceruleText}</div>
</div>


{details.ruleSpecificDataJson && (
  <>
    <h4>Rule Specific Details</h4>

    <div className="details-grid">

      {Object.entries(details.ruleSpecificDataJson).map(([key, value]) => (
        <div className="status-detail-item" key={key}>
          <div className="detail-label">
            {formatLabel(key)}
          </div>
          <div className="detail-value">
            {String(value)}
          </div>
        </div>
      ))}

    </div>
  </>
)}


<div className="status-detail-item">
<div className="detail-label">Installments</div>
<div className="detail-value">
{details.noofmonthlyinstallmentsforpaymentofconsolidatedadvance}
</div>
</div>

</div>

</>

)}



{appType === "withdrawl" && (

<>

<h4>Previous Withdrawal</h4>

<div className="details-grid">

<div className="status-detail-item">
<div className="detail-label">Prior Withdrawal For Same Purpose</div>
<div className="detail-value">
{details.ispriorwithdrawlforsamepurpose ? "Yes" : "No"}
</div>
</div>

<div className="status-detail-item">
<div className="detail-label">Previous Withdrawal Amount</div>
<div className="detail-value">₹{details.priorwithdrawlamount}</div>
</div>

<div className="status-detail-item">
<div className="detail-label">Previous Financial Year</div>
<div className="detail-value">{details.priorwithdrawlfinyear}</div>
</div>

</div>

</>

)}

</div>

{/* RIGHT PANEL */}
<div className="status-right-panel">

<h3>Application Progress</h3>

{trail.length === 0 && (
<p>No actions yet</p>
)}

{trail.map((t, i) => (
<div key={i} className="trail-box">

<div>
<span className="trail-role">{t.role}</span>
<span className="trail-action"> → {t.action}</span>
<span className="trail-time">{t.time}</span>
</div>

<div className="remarks">{t.remarks}</div>

</div>
))}

</div>

</div>

</td>
</tr>
)}

</React.Fragment>
);

})}

</tbody>

        </table>
      </div>

    </div>
  );
}

export default WithdrawlApplicationStatus;