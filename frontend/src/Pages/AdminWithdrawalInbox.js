import React, { useEffect, useState } from "react";
import api from "../api/axios";

const AdminWithdrawalInbox = () => {
  const [applications, setApplications] = useState([]);
  const [remarks, setRemarks] = useState({});
  const [error, setError] = useState("");

  // TEMP – acting role (will come from parent app later)
  const CURRENT_ROLE = "ADMIN";

  useEffect(() => {
    loadInbox();
  }, []);

  const loadInbox = async () => {
    try {
      const res = await api.get("/gpf/withdrawal/inbox", {
        params: { role: CURRENT_ROLE }
      });
      setApplications(res.data);
    } catch (err) {
      console.error(err);
      alert("Failed to load admin inbox");
    }
  };

  const handleVerify = async (applicationId) => {
    if (!remarks[applicationId] || !remarks[applicationId].trim()) {
      setError("Remarks are mandatory");
      return;
    }

    try {
      await api.post(`/gpf/withdrawal/${applicationId}/admin-verify`, {
        remarks: remarks[applicationId]
      });

      alert("Application verified and sent to Cash");

      setRemarks({});
      setError("");
      loadInbox();

    } catch (err) {
      alert(err.response?.data || "Verification failed");
    }
  };

  return (
    <div className="container">
      <h2>ADMIN – GPF Withdrawal Inbox</h2>

      {error && <div className="error-summary">{error}</div>}

      <table className="rule-table">
        <thead>
          <tr>
            <th>Application ID</th>
            <th>Employee Code</th>
            <th>Employee Name</th>
            <th>Amount</th>
            <th>Date</th>
            <th>Status</th>
            <th>Remarks</th>
            <th>Action</th>
          </tr>
        </thead>

        <tbody>
          {applications.length === 0 && (
            <tr>
              <td colSpan="8" style={{ textAlign: "center" }}>
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
                <input
                  type="text"
                  value={remarks[app.applicationId] || ""}
                  onChange={(e) =>
                    setRemarks(prev => ({
                      ...prev,
                      [app.applicationId]: e.target.value
                    }))
                  }
                  placeholder="Enter remarks"
                />
              </td>

              <td>
                {CURRENT_ROLE === "ADMIN" && app.canVerify && (
                  <button
                    className="rule-btn"
                    onClick={() => handleVerify(app.applicationId)}
                  >
                    Verify & Send to Cash
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default AdminWithdrawalInbox;
