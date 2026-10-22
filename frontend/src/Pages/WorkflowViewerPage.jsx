import React, { useEffect, useState } from "react";
import api from "../api/axios";
import "../styles/WorkflowViewerPage.css";
function WorkflowViewerPage() {

  const [workflows, setWorkflows] = useState([]);
  const [transitions, setTransitions] = useState([]);
  const [selectedWorkflow, setSelectedWorkflow] = useState(null);
const roles = [];

transitions.forEach((t, i) => {
  if (i === 0) {
    roles.push({
      role: t.fromRole,
      divName: null
    });
  }

  roles.push({
    role: t.toRole,
    divName: t.divName
  });
});

useEffect(() => {
    api.get("/workflow/list").then(res => setWorkflows(res.data)).catch(console.error);
      
}, []);

console.log(workflows);
  const loadTransitions = async (workflowId) => {

    setSelectedWorkflow(workflowId);

    try {
      const res = await api.get(`/workflow/transitions/${workflowId}`);
      setTransitions(res.data);
       console.log("API response:", res.data);
    console.log("Division names:", res.data.map(t => t.divName));
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="workflow-container">

      <h2>Workflow Configuration</h2>

      <table className="workflow-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Workflow Name</th>
            <th>Description</th>
            <th>Division</th>
          </tr>
        </thead>

        <tbody>
          {workflows.map(w => (
            <tr key={w.workflowId}
                onClick={() => loadTransitions(w.workflowId)}>
              <td>{w.workflowId}</td>
              <td>{w.workflowName}</td>
              <td>{w.description}</td>
              <td>{w.divName}</td>
            </tr>
          ))}
        </tbody>  
      </table>

{selectedWorkflow && (
  <div className="workflow-vertical">

    {roles.map((item, index) => (
      <React.Fragment key={index}>

        <div className="workflow-vertical-step">
          <div className="workflow-role">
            {item.role}
          </div>

          {item.divName && (
            <div className="workflow-division">
              {item.divName}
            </div>
          )}
        </div>

        {index !== roles.length - 1 && (
          <div className="workflow-vertical-line">
            ↓
          </div>
        )}

      </React.Fragment>
    ))}

  </div>
)}

    </div>
  );
}

export default WorkflowViewerPage;