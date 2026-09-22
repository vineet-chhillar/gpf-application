import React, { useEffect, useState } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";
import "../styles/GpfWorkflowPageworkflow.css";

const GpfWorkflowInboxPage = () => {

  const [applications, setApplications] = useState([]);
  const [appType, setAppType] = useState("withdrawl");

  const navigate = useNavigate();

  useEffect(() => {

    const url =
      appType === "withdrawl"
        ? "/gpf-withdrawl/inbox"
        : "/gpf-advance/inbox";

    api.get(url)
      .then(res => setApplications(res.data))
      .catch(err => console.error(err));

  }, [appType]);

  return (
    <div className="workflow-container">

      <div className="type-selector">

        <label className={appType === "withdrawl" ? "active" : ""}>
          <input
            type="radio"
            checked={appType === "withdrawl"}
            onChange={() => setAppType("withdrawl")}
          />
          <span>Withdrawal</span>
        </label>

        <label className={appType === "advance" ? "active" : ""}>
          <input
            type="radio"
            checked={appType === "advance"}
            onChange={() => setAppType("advance")}
          />
          <span>Advance</span>
        </label>

      </div>

      <div className="workflow-list"  style={{
    width: "95%"
  }}>

        <div className="workflow-table-wrapper" style={{
      width: "100%"
    }}>

          <table className="workflow-table" style={{
        width: "100%"
      }}>

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

              {applications.map(app => (

                <tr
                  key={app.applicationId}
                  style={{ cursor: "pointer" }}
                  onClick={() =>
                    navigate(`/workflow/${appType}/${app.applicationId}/${app.empCode}`)
                  }
                >
                  <td>{app.empCode}</td>

                  <td>
  <span className="status-pending">
    {app.pendingWithRole}
  </span>
</td>

                  
                  <td>{app.employeeName}</td>
                  <td>{app.designation}</td>
                  <td>₹{app.amount}</td>

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

      </div>

    </div>
  );
};

export default GpfWorkflowInboxPage;