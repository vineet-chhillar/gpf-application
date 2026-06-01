import React, { useEffect, useState } from "react";
import api from "../api/axios";
import "../styles/WithdrawlRuleMaster.css";


const WithdrawlRuleMaster = () => {
  const [rules, setRules] = useState([]);
  const [form, setForm] = useState({
    ruleCode: "",
    withdrawlReason: "",     // ✅ NEW
    ruleDescription: "",
    maxPercentage: "",
    maxMonthsPay: "",
    requiresVehicleCost: false,
    minServiceYears: "",
    retirementWindowYrs: ""
  });

  useEffect(() => {
    loadRules();
  }, []);

  const loadRules = async () => {
    const res = await api.get("/gpf/withdrawal-rules");
    setRules(res.data);
  };
const [messageBox, setMessageBox] = useState({
  open: false,
  title: "",
  message: "",
  onConfirm: null
});
  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm(prev => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value
    }));
  };

  const handleSubmit = async () => {
  try {
    await api.post("/gpf/withdrawal-rules", form);
    alert("Rule saved successfully");
    loadRules();
  } catch (err) {
    if (err.response?.status === 409) {
      alert(err.response.data); // "Rule Code already exists"
    } else {
      alert("Error saving rule");
    }
  }
};

const toggleRule = (ruleId, ruleCode, isActive) => {

  const action = isActive ? "Deactivate" : "Activate";

  setMessageBox({
    open: true,
    title: `${action} Rule`,
    message: `Do you want to ${action.toLowerCase()} Rule Code '${ruleCode}'?`,
    onConfirm: async () => {

      await api.patch(
        `/gpf/withdrawal-rules/${ruleId}/toggle`
      );

      loadRules();

      setMessageBox(prev => ({
        ...prev,
        open: false
      }));
    }
  });
};

  return (
    <>
    <div className="container">
      <h2>Withdrawal Rule Master</h2>

      <div className="rule-form">

  <div className="rule-form-group">
    <label>Rule Code</label>
    <input
      className="rule-input"
      name="ruleCode"
      placeholder="Rule Code"
      onChange={handleChange}
    />
  </div>

<div className="rule-form-group">
  <label>Withdrawal Reason</label>
  <input
    className="rule-input"
    name="withdrawlReason"
    placeholder="Illness / Education / Marriage"
    onChange={handleChange}
  />
</div>


  <div className="rule-form-group">
    <label>Description</label>
    <input
      className="rule-input"
      name="ruleDescription"
      placeholder="Description"
      onChange={handleChange}
    />
  </div>

  <div className="rule-form-group">
    <label>Max %</label>
    <input
      className="rule-input"
      name="maxPercentage"
      placeholder="Max %"
      onChange={handleChange}
    />
  </div>

  <div className="rule-form-group">
    <label>Max Months Pay</label>
    <input
      className="rule-input"
      name="maxMonthsPay"
      placeholder="Months Pay"
      onChange={handleChange}
    />
  </div>

  <div className="rule-form-group">
    <label>Min Service Years</label>
    <input
      className="rule-input"
      name="minServiceYears"
      placeholder="Years"
      onChange={handleChange}
    />
  </div>

  <div className="rule-form-group">
    <label>Retirement Window (Years)</label>
    <input
      className="rule-input"
      name="retirementWindowYrs"
      placeholder="Years"
      onChange={handleChange}
    />
  </div>

  <div className="rule-form-group checkbox-group">
    <label className="rule-checkbox">
      <input
        type="checkbox"
        name="requiresVehicleCost"
        onChange={handleChange}
      />
      Requires Vehicle Cost
    </label>
  </div>

</div>

{/* ✅ BUTTON OUTSIDE GRID */}
<div className="rule-action-row">
  <button className="rule-btn" onClick={handleSubmit}>
    Save Rule
  </button>
</div>



      

      <hr />

      <table className="rule-table">
  <thead>
    <tr>
      <th>Rule Code</th>
      <th>Withdrawal Reason</th>

      <th>Description</th>
      <th>Max %</th>
      <th>Max Months Pay</th>
      <th>Vehicle Cost Req.</th>
      <th>Min Service (Years)</th>
      <th>Retirement Window (Years)</th>
      <th>Active</th>
      <th>Created By</th>
      <th>Created At</th>
      
    </tr>
  </thead>

  <tbody>
    {Array.isArray(rules) && rules.map((r) => (
      <tr key={r.ruleId}>
        <td>{r.ruleCode}</td>
        <td>{r.withdrawlReason ?? "-"}</td>

        <td>{r.ruleDescription}</td>
        <td>{r.maxPercentage ?? "-"}</td>
        <td>{r.maxMonthsPay ?? "-"}</td>
        <td>{r.requiresVehicleCost ? "Yes" : "No"}</td>
        <td>{r.minServiceYears ?? "-"}</td>
        <td>{r.retirementWindowYrs ?? "-"}</td>
        <td>
  <label className="switch">
    <input
      type="checkbox"
      checked={r.isActive}
      onChange={() => toggleRule(
      r.ruleId,
      r.ruleCode,
      r.isActive
    )}
    />
    <span className="slider"></span>
  </label>
</td>

        <td>{r.createdby}</td>
        <td>
          {r.createdat
            ? new Date(r.createdat).toLocaleDateString()
            : "-"}
        </td>
        
      </tr>
    ))}
  </tbody>
</table>

    </div>
    {messageBox.open && (
  <div className="modal-overlay">
    <div className="message-modal">

      <div className="message-modal-header">
        {messageBox.title}
      </div>

      <div className="message-modal-body">
        {messageBox.message}
      </div>

      <div className="message-modal-footer">

        <button
          className="btn-cancel"
          onClick={() =>
            setMessageBox(prev => ({
              ...prev,
              open: false
            }))
          }
        >
          No
        </button>

        <button
          className="btn-confirm"
          onClick={messageBox.onConfirm}
        >
          Yes
        </button>

      </div>

    </div>
  </div>
)}
</>
  );
};

export default WithdrawlRuleMaster;
