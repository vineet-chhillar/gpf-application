import React, { useState } from "react";
import StatusTable from "./StatusTable.jsx";
import StatusForm from "./StatusForm";
import { useEffect } from "react";
import "../../styles/form-controls.css";
import {
  fetchStatuses,
  createStatus,
  updateStatus,
  toggleStatusActive
} from "../../api/statusMasterApi";



const StatusMasterPage = () => {
  const [statuses, setStatuses] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [editingStatus, setEditingStatus] = useState(null);
const [formError, setFormError] = useState(null);
const [fieldErrors, setFieldErrors] = useState({});
const handleToggleActive = async (row) => {
  await toggleStatusActive(row.id);
  loadStatuses();
};

useEffect(() => {
    console.log("StatusMasterPage mounted");
  loadStatuses();
}, []);


const handleSave = async (payload) => {
  try {
    setFormError(null);
    setFieldErrors({});

    if (editingStatus) {
      await updateStatus(editingStatus.id, payload);
    } else {
      await createStatus(payload);
    }

    setShowForm(false);
    setEditingStatus(null);
    loadStatuses();
  } catch (err) {
    // Validation errors (400)
    if (err.errors) {
      setFieldErrors(err.errors);
    }
    // Duplicate / other errors
    else if (err.message) {
      setFormError(err.message);
    }
  }
};
const loadStatuses = async () => {
  const data = await fetchStatuses();
  console.log("Data from backend:", data);
  setStatuses(data);
};





  return (
    <div>
        <div className="page-header">
  <span className="page-header-icon"></span>
  <span className="page-header-text">Status Master</span>
</div>

         <StatusTable
  data={statuses}
  onEdit={(row) => {
    setEditingStatus(row);
    setShowForm(true);
  }}
  onToggleActive={handleToggleActive}
/>
      

      <button className="btn btn-primary" onClick={() => {
        setEditingStatus(null);
        setShowForm(true);
      }}>
        + Add New Status
      </button>

     


      {showForm && (
  <StatusForm
    initialData={editingStatus}
    onClose={() => setShowForm(false)}
    onSave={handleSave}
    error={formError}
    fieldErrors={fieldErrors}
  />
)}

    </div>
  );
};

export default StatusMasterPage;
