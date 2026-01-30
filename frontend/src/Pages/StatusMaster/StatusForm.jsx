import { useState, useEffect } from "react";
import "../../styles/form-controls.css";

const StatusForm = ({initialData,onClose,onSave,error,fieldErrors}) => {
  const [statusCode, setStatusCode] = useState("");
  const [isFinal, setIsFinal] = useState(false);
  const [isActive, setIsActive] = useState(true);



  useEffect(() => {
  if (initialData) {
    setStatusCode(initialData.statusCode);
    setIsFinal(initialData.final);
    setIsActive(initialData.active);
  }
}, [initialData]);


  const handleSubmit = () => {
  const payload = {
    statusCode: statusCode.toUpperCase(),
    isFinal,
    isActive
  };

  onSave(payload);
};


  return (
    <div className="modal">
      <div className="form-title">
        {error && <div className="form-error">{error}</div>}

  <span className="form-title-icon">
    {initialData ? "✏️" : "➕"}
  </span>
  <span className="form-title-text">
    {initialData ? "Edit Status" : "Add Status"}
  </span>
</div>


      <div className="form-row-center">
  {/* Status Code */}
  <div className="form-group">
    <label className="form-label">Status Code</label>
   <input
  className="form-input"
  value={statusCode}
  onChange={(e) => setStatusCode(e.target.value)}
  disabled={!!initialData}
/>

{fieldErrors?.statusCode && (
  <div className="field-error">
    {fieldErrors.statusCode}
  </div>
)}

  </div>

  {/* Is Final */}
  <label className="form-checkbox">
    <input
      type="checkbox"
      checked={isFinal}
      onChange={(e) => setIsFinal(e.target.checked)}
    />
    <span>Is Final</span>
  </label>

  {/* Is Active */}
  <label className="form-checkbox">
    <input
      type="checkbox"
      checked={isActive}
      onChange={(e) => setIsActive(e.target.checked)}
    />
    <span>Is Active</span>
  </label>
</div>


<div
  style={{
    marginTop: "20px",
    display: "flex",
    justifyContent: "center",   // 🔑 centers buttons
    gap: "12px"
  }}
>
  <button className="btn btn-primary" onClick={handleSubmit}>
    Save
  </button>
  <button className="btn btn-secondary" onClick={onClose}>
    Cancel
  </button>
</div>

    </div>
  );
};

export default StatusForm;
