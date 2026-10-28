import React, { useEffect, useState } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";
import "../styles/GpfWorkflowPageworkflow.css";

const GpfWorkflowInboxPage = () => {
  const [applications, setApplications] = useState([]);
  const [appType, setAppType] = useState("withdrawl");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const navigate = useNavigate();

  useEffect(() => {
    let cancelled = false;

    const fetchInbox = async () => {
      setLoading(true);
      setError("");
      setApplications([]);

      const url =
        appType === "withdrawl"
          ? "/gpf-withdrawl/inbox"
          : "/gpf-advance/inbox";

      try {
        const res = await api.get(url, {
          withCredentials: true,
        });

        if (!cancelled) {
          setApplications(Array.isArray(res.data) ? res.data : []);
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err.response?.status === 401 || err.response?.status === 403
              ? "Your session may have expired, or you are not authorized to view this inbox."
              : "Unable to load pending applications. Please try again."
          );
          setApplications([]);
        }

        console.error("Workflow inbox error:", err);
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    fetchInbox();

    return () => {
      cancelled = true;
    };
  }, [appType]);

  return (
    <div className="workflow-container">
      <div className="type-selector">
        <label className={appType === "withdrawl" ? "active" : ""}>
          <input
            type="radio"
            name="applicationType"
            value="withdrawl"
            checked={appType === "withdrawl"}
            onChange={() => setAppType("withdrawl")}
          />
          <span>Withdrawal</span>
        </label>

        <label className={appType === "advance" ? "active" : ""}>
          <input
            type="radio"
            name="applicationType"
            value="advance"
            checked={appType === "advance"}
            onChange={() => setAppType("advance")}
          />
          <span>Advance</span>
        </label>
      </div>

      <div className="workflow-list" style={{ width: "95%" }}>
        {loading && <p>Loading pending applications...</p>}

        {!loading && error && (
          <p className="error-message">{error}</p>
        )}

      {!loading && !error && applications.length === 0 && (
  <p className="no-pending-applications">
    No pending applications for your role and division.
  </p>
)}

        {!loading && !error && applications.length > 0 && (
          <div className="workflow-table-wrapper" style={{ width: "100%" }}>
            <table className="workflow-table" style={{ width: "100%" }}>
              <thead>
                <tr>
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
                {applications.map((app) => (
                  <tr
                    key={app.applicationId}
                    style={{ cursor: "pointer" }}
                    onClick={() =>
                      navigate(
                        `/workflow/${appType}/${app.applicationId}/${app.empCode}`
                      )
                    }
                  >
                    <td>{app.empCode}</td>

                    <td>
                      <span className="status-pending">
                        {app.currentDivisionName &&
                        app.currentDivisionName !== "-"
                          ? `${app.pendingWithRole} (${app.currentDivisionName})`
                          : app.pendingWithRole}
                      </span>
                    </td>

                    <td>{app.employeeName}</td>
                    <td>{app.designation}</td>
                    <td>
                      {app.amount != null
                        ? `₹${app.amount}`
                        : ""}
                    </td>

                    <td>
                      {app.applicationDate
                        ? new Date(
                            app.applicationDate
                          ).toLocaleDateString()
                        : ""}
                    </td>

                    <td>{app.purpose}</td>
                    <td>{app.applicationId}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default GpfWorkflowInboxPage;
