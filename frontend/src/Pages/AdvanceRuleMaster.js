import React, { useEffect, useState } from "react";
import api from "../api/axios";
import "../styles/WithdrawlRuleMaster.css";

const AdvanceRuleMaster = () => {

  const [rules, setRules] = useState([]);

  const [form, setForm] = useState({
    ruleCode: "",
    advanceReason: "",
    ruleDescription: "",
    maxPercentage: "",
    maxMonthsPay: "",
    minServiceYears: "",
    isSpecialCase: false
  });

  useEffect(() => {
    loadRules();
  }, []);

  const loadRules = async () => {
    const res = await api.get("/gpf/advance-rules");
    setRules(res.data);
  };

  const handleChange = (e) => {

    const { name, value, type, checked } = e.target;

    setForm(prev => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value
    }));

  };

  const handleSubmit = async () => {

    try {

      await api.post("/gpf/advance-rules", form);

      alert("Advance Rule saved successfully");

      loadRules();

    } catch (err) {

      if (err.response?.status === 409) {
        alert(err.response.data);
      } else {
        alert("Error saving rule");
      }

    }

  };

  const toggleRule = async (ruleId) => {

    await api.patch(`/gpf/advance-rules/${ruleId}/toggle`);

    loadRules();

  };

  return (
    <div className="container">

      <h2>Advance Rule Master</h2>

      <div className="rule-form">

        <div className="rule-form-group">
          <label>Rule Code</label>
          <input className="rule-input" name="ruleCode" onChange={handleChange}/>
        </div>

        <div className="rule-form-group">
          <label>Advance Reason</label>
          <input className="rule-input" name="advanceReason" onChange={handleChange}/>
        </div>

        <div className="rule-form-group">
          <label>Description</label>
          <input className="rule-input" name="ruleDescription" onChange={handleChange}/>
        </div>

        <div className="rule-form-group">
          <label>Max %</label>
          <input className="rule-input" name="maxPercentage" onChange={handleChange}/>
        </div>

        <div className="rule-form-group">
          <label>Max Months Pay</label>
          <input className="rule-input" name="maxMonthsPay" onChange={handleChange}/>
        </div>

        <div className="rule-form-group">
          <label>Min Service Years</label>
          <input className="rule-input" name="minServiceYears" onChange={handleChange}/>
        </div>

        <div className="rule-form-group checkbox-group">
          <label className="rule-checkbox">
            <input
              type="checkbox"
              name="isSpecialCase"
              onChange={handleChange}
            />
            Special Case (90%)
          </label>
        </div>

      </div>

      <div className="rule-action-row">
        <button className="rule-btn" onClick={handleSubmit}>
          Save Rule
        </button>
      </div>

      <hr />

      <table className="status-table">

        <thead>
          <tr>
            <th>Rule Code</th>
            <th>Advance Reason</th>
            <th>Description</th>
            <th>Max %</th>
            <th>Max Months Pay</th>
            <th>Special Case</th>
            <th>Min Service Years</th>
            <th>Active</th>
            <th>Created By</th>
            <th>Created At</th>
          </tr>
        </thead>

        <tbody>

          {rules.map(r => (

            <tr key={r.ruleId}>

              <td>{r.ruleCode}</td>
              <td>{r.advanceReason}</td>
              <td>{r.ruleDescription}</td>
              <td>{r.maxPercentage ?? "-"}</td>
              <td>{r.maxMonthsPay ?? "-"}</td>
              <td>{r.isSpecialCase ? "Yes" : "No"}</td>
              <td>{r.minServiceYears ?? "-"}</td>

              <td>
                <label className="switch">
                  <input
                    type="checkbox"
                    checked={r.isActive}
                    onChange={() => toggleRule(r.ruleId)}
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
  );
};

export default AdvanceRuleMaster;