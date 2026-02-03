import React, { useEffect, useState } from "react";
import api from "../api/axios";
import "../styles/GpfWithdrawlForm.css";

const EmployeeWithdrawalInbox = () => {

  const EMP_CODE = "EMP010"; // TEMP – from parent app later

  const [applications, setApplications] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [details, setDetails] = useState(null);
  const [trail, setTrail] = useState([]);

  useEffect(() => {
    loadInbox();
  }, []);

  const loadInbox = async () => {
    const res = await api.get(
      "/gpf/withdrawal/employee/inbox",
      { params: { empcode: EMP_CODE } }
    );
    setApplications(res.data);
  };

  const openDetails = async (id) => {
    const [detailsRes, trailRes] = await Promise.all([
      api.get(`/gpf/withdrawal/${id}`),
      api.get(`/gpf/withdrawal/${id}/status-trail`)
    ]);

    setDetails(detailsRes.data);
    setTrail(trailRes.data);
    setSelectedId(id);
  };

  const closeModal = () => {
    setSelectedId(null);
    setDetails(null);
    setTrail([]);
  };

  return (
    <div className="container">
      <h2>My GPF Withdrawal Applications</h2>

      <table className="rule-table">
        <thead>
          <tr>
            <th>Application ID</th>
            <th>Amount</th>
            <th>Date</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {applications.map(app => (
            <tr key={app.applicationId}>
              <td>{app.applicationId}</td>
              <td>{app.amountRequested}</td>
              <td>{app.dateOfApplication}</td>
              <td>{app.statusCode}</td>
              <td>
                <button
                  className="rule-btn"
                  onClick={() => openDetails(app.applicationId)}
                >
                  View
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* ===== MODAL ===== */}
      {details && (
        <div className="modal-overlay">
          <div className="modal large">

            <h3>Application #{details.applicationId}</h3>

            {/* ===== STATUS TRAIL ===== */}
            <div className="card">
              <div className="card-header">
                📌 Status Timeline
              </div>
              <div className="card-body">
                {trail.map((t, i) => (
                  <div key={i} className="timeline-row">
                    <b>{t.statusCode}</b>
                    <div>{t.actionBy}</div>
                    <small>{t.actionAt}</small>
                    {t.remarks && <div>📝 {t.remarks}</div>}
                  </div>
                ))}
              </div>
            </div>

            {/* ===== EMPLOYEE ===== */}
            <div className="card">
              <div className="card-header">👤 Employee Details</div>
              <div className="card-body">
                <p><b>Name:</b> {details.empName}</p>
                <p><b>Designation:</b> {details.designation}</p>
                <p><b>Division:</b> {details.division}</p>
              </div>
            </div>

            {/* ===== WITHDRAWAL ===== */}
            <div className="card">
              <div className="card-header">📝 Withdrawal Details</div>
              <div className="card-body">
                <p><b>Amount:</b> {details.amountRequested}</p>
                <p><b>Purpose:</b> {details.purposeOfWithdrawal}</p>
                <p><b>Rule:</b> {details.withdrawalRule}</p>
              </div>
            </div>

            {/* ===== FINANCIAL ===== */}
            <div className="card">
              <div className="card-header">💰 Financial Snapshot</div>
              <div className="card-body">
                <p><b>Basic Pay:</b> {details.basicPay}</p>
                <p><b>Outstanding:</b> {details.outstandingBalance}</p>
                <p><b>Net Balance:</b> {details.netBalance}</p>
              </div>
            </div>

            <div className="modal-actions">
              <button className="rule-btn secondary" onClick={closeModal}>
                Close
              </button>
            </div>

          </div>
        </div>
      )}
    </div>
  );
};

export default EmployeeWithdrawalInbox;
