import React, { useEffect, useState } from "react";
import api from "../api/axios";
import "../styles/WorkflowViewerPage.css";
function WorkflowViewerPage() {

  const [workflows, setWorkflows] = useState([]);
  const [transitions, setTransitions] = useState([]);
  const [selectedWorkflow, setSelectedWorkflow] = useState(null);

  const roles = [];

transitions.forEach((t, i) => {
  if (i === 0) roles.push(t.fromRole);
  roles.push(t.toRole);
});


  useEffect(() => {
    api.get("/workflow/list")
      .then(res => setWorkflows(res.data))
      .catch(console.error);
  }, []);

  const loadTransitions = async (workflowId) => {

    setSelectedWorkflow(workflowId);

    try {
      const res = await api.get(`/workflow/transitions/${workflowId}`);
      setTransitions(res.data);
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
          </tr>
        </thead>

        <tbody>
          {workflows.map(w => (
            <tr key={w.workflowId}
                onClick={() => loadTransitions(w.workflowId)}>
              <td>{w.workflowId}</td>
              <td>{w.workflowName}</td>
              <td>{w.description}</td>
            </tr>
          ))}
        </tbody>
      </table>

{selectedWorkflow && (
<div className="workflow-vertical">

  {roles.map((role, index) => (
    <React.Fragment key={index}>

      <div className="workflow-vertical-step">
        {role}
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