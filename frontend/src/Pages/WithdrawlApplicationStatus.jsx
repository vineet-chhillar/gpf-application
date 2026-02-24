import React, { useEffect, useState } from "react";
import "../styles/WithdrawlApplicationStatus.css";
import api from "../api/axios";
/* temporary until login integration */
const HARDCODED_EMPCODE = "EMP021";

function WithdrawlApplicationStatus() {

  const [expandedRow, setExpandedRow] = useState(null);

  {/*const [application, setApplication] = useState({
    master: {},
    details: {},
    trail: []
  });*/}

const [applications, setApplications] = useState([]);

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
            </tr>
          </thead>

          <tbody>

{applications.map((app, index) => {

const master = app.master || {};
const details = app.details || {};
const trail = app.trail || [];

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
</tr>

{expandedRow === index && (
<tr className="expanded-row">
<td colSpan="6">

<div className="expanded-content">

{/* LEFT PANEL */}
<div className="left-panel">

<h3>Employee Information</h3>

<p><b>Mobile:</b> {master.empmobileno}</p>
<p><b>Email:</b> {master.empemailid}</p>
<p><b>Date of Joining:</b> {master.dateofjoining}</p>
<p><b>Retirement:</b> {master.dateofsuperannuation}</p>

<hr />

<h3>GPF Account Summary</h3>

<p><b>Basic Pay:</b> ₹{details.basicpay}</p>
<p><b>Outstanding Balance Date:</b> {details.dateofoutstandingbalance}</p>
<p><b>Outstanding Balance:</b> ₹{details.outstandingbalance}</p>
<p><b>Credit From:</b> {details.creditfromdate}</p>
<p><b>Credit To:</b> {details.credittodate}</p>
<p><b>Total Credit Amount:</b> ₹{details.totalcreditamount}</p>
<p><b>Refund After Balance Date:</b> ₹{details.refundafterdateofoutstandingbalance}</p>

<hr />

<h3>Withdrawal Calculation</h3>

<p><b>Withdraw From:</b> {details.withdrawlfromdate}</p>
<p><b>Withdraw To:</b> {details.withdrawltodate}</p>
<p><b>Total Withdrawal Amount:</b> ₹{details.totalwithdrawlamount}</p>
<p><b>Net Balance:</b> ₹{details.netbalance}</p>

<hr />

<h3>Withdrawal Request</h3>

<p><b>Requested Amount:</b> ₹{details.amountofwithdrawlrequested}</p>
<p><b>Purpose:</b> {details.purposeofwithdrawl}</p>
<p><b>Withdrawal Rule:</b> {details.withdrawlrule}</p>
<p><b>Application Date:</b>
{details.dateofapplication
  ? new Date(details.dateofapplication).toLocaleDateString()
  : ""}
</p>

<hr />

<h3>Previous Withdrawal</h3>

<p><b>Previously Withdrawn For Same Purpose:</b>
{details.ispriorwithdrawlforsamepurpose ? "Yes" : "No"}
</p>

<p><b>Previous Withdrawal Amount:</b> ₹{details.priorwithdrawlamount}</p>
<p><b>Previous Financial Year:</b> {details.priorwithdrawlfinyear}</p>

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