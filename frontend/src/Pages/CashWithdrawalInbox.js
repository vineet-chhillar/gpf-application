import React, { useEffect, useState } from "react";
import api from "../api/axios";
import "../styles/GpfWithdrawlForm.css";

const CashWithdrawalInbox = () => {
  const [applications, setApplications] = useState([]);
  const [selectedAppId, setSelectedAppId] = useState(null);
  const [details, setDetails] = useState(null);
  const [remarks, setRemarks] = useState("");
  const [error, setError] = useState("");

  const [showEmp, setShowEmp] = useState(true);
  const [showWithdrawal, setShowWithdrawal] = useState(true);
  const [showFinancial, setShowFinancial] = useState(true);

  const CURRENT_ROLE = "CASH";

  useEffect(() => {
    loadInbox();
  }, []);

  const loadInbox = async () => {
    try {
      const res = await api.get("/gpf/withdrawal/inbox", {
        params: { role: CURRENT_ROLE }
      });
      setApplications(res.data);
    } catch {
      alert("Failed to load cash inbox");
    }
  };

  const openDetails = async (applicationId) => {
    try {
      setShowEmp(true);
      setShowWithdrawal(true);
      setShowFinancial(true);

      const res = await api.get(`/gpf/withdrawal/${applicationId}`);
      setDetails(res.data);
      setSelectedAppId(applicationId);
      setRemarks("");
      setError("");
    } catch {
      alert("Failed to load details");
    }
  };

  const closeModal = () => {
    setSelectedAppId(null);
    setDetails(null);
    setRemarks("");
    setError("");
  };

  const handleVerify = async () => {
    if (!remarks.trim()) {
      setError("Remarks are mandatory");
      return;
    }

    try {
      await api.post(
  `/gpf/withdrawal/${selectedAppId}/cash-verify`,
  { remarks }
);


      alert("Application verified and sent back to Admin");
      closeModal();
      loadInbox();

    } catch (err) {
      alert(err.response?.data || "Verification failed");
    }
  };

  return (
    <div className="container">
      <h2>CASH – GPF Withdrawal Inbox</h2>

      <table className="rule-table">
        <thead>
          <tr>
            <th>Application ID</th>
            <th>Emp Code</th>
            <th>Emp Name</th>
            <th>Amount</th>
            <th>Date</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>

        <tbody>
          {applications.length === 0 && (
            <tr>
              <td colSpan="7" align="center">
                No applications pending
              </td>
            </tr>
          )}

          {applications.map(app => (
            <tr key={app.applicationId}>
              <td>{app.applicationId}</td>
              <td>{app.empCode}</td>
              <td>{app.empName}</td>
              <td>{app.amountRequested}</td>
              <td>{app.dateOfApplication}</td>
              <td>{app.statusCode}</td>
              <td>
                <button
                  className="rule-btn"
                  onClick={() => openDetails(app.applicationId)}
                >
                  View Details
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* ===== MODAL (same as ADMIN, label changed) ===== */}
      {details && (
        <div className="modal-overlay">
          <div className="modal">

            <h3>GPF Withdrawal Application</h3>

            {/* Employee */}
            <div className="card">
              <div className="card-header" onClick={() => setShowEmp(p => !p)}>
                <span>👤 Employee Details</span>
                <span>{showEmp ? "−" : "+"}</span>
              </div>
              {showEmp && (
                <div className="card-body">
                  <p><b>Code:</b> {details.empCode}</p>
                  <p><b>Name:</b> {details.empName}</p>
                  <p><b>Designation:</b> {details.designation}</p>
                  <p><b>Division:</b> {details.division}</p>
                </div>
              )}
            </div>

            {/* Withdrawal */}
            {/* Withdrawal */}
<div className="card">
  <div
    className="card-header"
    onClick={() => setShowWithdrawal(p => !p)}
  >
    <span>📝 Withdrawal Details</span>
    <span>{showWithdrawal ? "−" : "+"}</span>
  </div>

  {showWithdrawal && (
    <div className="card-body">
      <p><b>Amount Requested:</b> {details.amountRequested}</p>
      <p><b>Purpose:</b> {details.purposeOfWithdrawal}</p>
      <p><b>Rule:</b> {details.withdrawalRule}</p>

      <hr />

      <p><b>Credit From:</b> {details.creditFromDate}</p>
      <p><b>Credit To:</b> {details.creditToDate}</p>
      <p><b>Total Credit Amount:</b> {details.totalCreditAmount}</p>

      <hr />

      <p><b>Withdrawal From:</b> {details.withdrawlFromDate}</p>
      <p><b>Withdrawal To:</b> {details.withdrawlToDate}</p>
      <p><b>Total Withdrawal Amount:</b> {details.totalWithdrawlAmount}</p>

      <p><b>Date of Application:</b> {details.dateOfApplication}</p>
    </div>
  )}
</div>


            {/* Financial */}
           {/* Financial */}
<div className="card">
  <div
    className="card-header"
    onClick={() => setShowFinancial(p => !p)}
  >
    <span>💰 Financial Snapshot</span>
    <span>{showFinancial ? "−" : "+"}</span>
  </div>

  {showFinancial && (
    <div className="card-body">
      <p><b>Basic Pay:</b> {details.basicPay}</p>
      <p><b>Outstanding Balance:</b> {details.outstandingBalance}</p>
      <p><b>Refund After Outstanding Balance:</b> {details.refundAfterOutstanding}</p>
      <p><b>Net Balance:</b> {details.netBalance}</p>
    </div>
  )}
</div>


            {/* Action */}
            <div className="card action-card">
              <h4>CASH Action</h4>

              {error && <div className="error-summary">{error}</div>}

              <textarea
                placeholder="Enter remarks (mandatory)"
                value={remarks}
                onChange={(e) => setRemarks(e.target.value)}
              />

              <div className="modal-actions">
                <button className="rule-btn" onClick={handleVerify}>
                  Verify & Send Back to Admin
                </button>

                <button className="rule-btn secondary" onClick={closeModal}>
                  Cancel
                </button>
              </div>
            </div>

          </div>
        </div>
      )}
    </div>
  );
};

export default CashWithdrawalInbox;
