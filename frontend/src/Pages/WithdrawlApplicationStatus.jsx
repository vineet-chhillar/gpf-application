import React, { useEffect, useState } from "react";
import "../styles/WithdrawlApplicationStatus.css";
import api from "../api/axios";
/* temporary until login integration */
const HARDCODED_EMPCODE = "EMP021";

function WithdrawlApplicationStatus() {

  const [expandedRow, setExpandedRow] = useState(null);
const [search, setSearch] = useState("");



  {/*const [application, setApplication] = useState({
    master: {},
    details: {},
    trail: []
  });*/}

const [applications, setApplications] = useState([]);
const filteredApps = applications.filter(a =>
  a.master?.empname?.toLowerCase().includes(search.toLowerCase()) ||
  a.master?.empcode?.toLowerCase().includes(search.toLowerCase())
);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
  const loadData = async () => {
    try {
      //const res = await api.get(
        //`/gpf-withdrawl/status/${HARDCODED_EMPCODE}`
      //);
      const res = await api.get("/gpf-withdrawl/status-all");
setApplications(res.data);
    } catch (err) {
      console.error("Failed to load workflow status", err);
    } finally {
      setLoading(false);
    }
  };
  loadData();
}, []);

  const toggleRow = (index) => {
    setExpandedRow(expandedRow === index ? null : index);
  };

  if (loading) {
    return <div className="workflow-container">Loading...</div>;
  }

  //const { master, details, trail } = application;

  return (
    <div className="workflow-container">

      <h2>GPF Withdrawal Application Status</h2>

<div className="dashboard-cards">

<div className="card">
<h3>Total Applications</h3>
<p>{applications.length}</p>
</div>

<div className="card">
<h3>Pending</h3>
<p>{applications.filter(a => a.currentOwnerRole !== "Completed").length}</p>
</div>

<div className="card">
<h3>Completed</h3>
<p>{applications.filter(a => a.currentOwnerRole === "Completed").length}</p>
</div>

</div>
<div className="search-container">
  <input
    type="text"
    className="search-input"
    placeholder="Search by name or employee code..."
    value={search}
    onChange={(e) => setSearch(e.target.value)}
  />
</div>
      <div className="workflow-table-wrapper">
        <table className="workflow-table">

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
  className={`main-row ${expandedRow === index ? "selected-row" : ""}`}
  onClick={() => toggleRow(index)}
>
  <td>{master.empcode}</td>
  <td>{master.empname}</td>
  <td>{master.designation}</td>
  <td>{master.empdivision}</td>
  <td>{details.basicpay}</td>
  <td>{details.netbalance}</td>

  <td>
<span className={
  app.currentOwnerRole === "Completed"
    ? "status-complete"
    : "status-pending"
}>
  {app.currentOwnerRole === "Completed" ? "Completed" : "Pending"}
</span>
</td>

<td><span className="pending-role">{app.currentOwnerRole}</span></td>
<td>{app.lastActionByRole}</td>
<td className="remarks-column">{app.lastRemarks}</td>
</tr>

{expandedRow === index && (
<tr className="expanded-row">
<td colSpan="10">

<div className="expanded-content">

{/* LEFT PANEL */}
<div className="left-panel">

<h4>Employee Information</h4>

<div className="details-grid">

<div className="detail-item">
<div className="detail-label">Mobile</div>
<div className="detail-value">{master.empmobileno}</div>
</div>

<div className="detail-item">
<div className="detail-label">Email</div>
<div className="detail-value">{master.empemailid}</div>
</div>

<div className="detail-item">
<div className="detail-label">Date of Joining</div>
<div className="detail-value">{master.dateofjoining}</div>
</div>

<div className="detail-item">
<div className="detail-label">Retirement</div>
<div className="detail-value">{master.dateofsuperannuation}</div>
</div>

</div>



<h4>GPF Account Summary</h4>

<div className="details-grid">

<div className="detail-item">
<div className="detail-label">Basic Pay</div>
<div className="detail-value">₹{details.basicpay}</div>
</div>

<div className="detail-item">
<div className="detail-label">Outstanding Balance Date</div>
<div className="detail-value">{details.dateofoutstandingbalance}</div>
</div>

<div className="detail-item">
<div className="detail-label">Outstanding Balance</div>
<div className="detail-value">₹{details.outstandingbalance}</div>
</div>

<div className="detail-item">
<div className="detail-label">Credit From</div>
<div className="detail-value">{details.creditfromdate}</div>
</div>

<div className="detail-item">
<div className="detail-label">Credit To</div>
<div className="detail-value">{details.credittodate}</div>
</div>

<div className="detail-item">
<div className="detail-label">Total Credit Amount</div>
<div className="detail-value">₹{details.totalcreditamount}</div>
</div>

<div className="detail-item">
<div className="detail-label">Refund After Balance Date</div>
<div className="detail-value">₹{details.refundafterdateofoutstandingbalance}</div>
</div>

</div>


<h4>Withdrawal Calculation</h4>

<div className="details-grid">

<div className="detail-item">
<div className="detail-label">Withdraw From</div>
<div className="detail-value">{details.withdrawlfromdate}</div>
</div>

<div className="detail-item">
<div className="detail-label">Withdraw To</div>
<div className="detail-value">{details.withdrawltodate}</div>
</div>

<div className="detail-item">
<div className="detail-label">Total Withdrawal Amount</div>
<div className="detail-value">₹{details.totalwithdrawlamount}</div>
</div>

<div className="detail-item">
<div className="detail-label">Net Balance</div>
<div className="detail-value">₹{details.netbalance}</div>
</div>

</div>



<h4>Withdrawal Request</h4>

<div className="details-grid">

<div className="detail-item">
<div className="detail-label">Requested Amount</div>
<div className="detail-value">₹{details.amountofwithdrawlrequested}</div>
</div>

<div className="detail-item detail-wide">
<div className="detail-label">Purpose</div>
<div className="detail-value">{details.purposeofwithdrawl}</div>
</div>

<div className="detail-item">
<div className="detail-label">Withdrawal Rule</div>
<div className="detail-value">{details.withdrawlrule}</div>
</div>

<div className="detail-item">
<div className="detail-label">Application Date</div>
<div className="detail-value">
{details.dateofapplication
  ? new Date(details.dateofapplication).toLocaleDateString()
  : ""}
</div>
</div>

</div>



<h4>Previous Withdrawal</h4>

<div className="details-grid">

<div className="detail-item">
<div className="detail-label">Prior Withdrawal For Same Purpose</div>
<div className="detail-value">
{details.ispriorwithdrawlforsamepurpose ? "Yes" : "No"}
</div>
</div>

<div className="detail-item">
<div className="detail-label">Previous Withdrawal Amount</div>
<div className="detail-value">₹{details.priorwithdrawlamount}</div>
</div>

<div className="detail-item">
<div className="detail-label">Previous Financial Year</div>
<div className="detail-value">{details.priorwithdrawlfinyear}</div>
</div>

</div>

</div>

{/* RIGHT PANEL */}
<div className="right-panel">

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