import React from "react";
import "../styles/GpfWorkflowPage.css";

/* ================= RULE INPUT ================= */
const RuleInput = ({
  label,
  name,
  type = "text",
  appId,
  isEditable,
  editData,
  handleEditChange
}) => {

  const val =
    editData?.[appId]?.ruleSpecificDataJson?.[name] ?? "";

  return (
    <div className="detail-item">
      <span className="label">{label}</span>

      {isEditable ? (
        <input
          type={type}
          value={val}
          onChange={(e) =>
            handleEditChange(
              appId,
              name,
              type === "number"
                ? Number(e.target.value)
                : e.target.value
            )
          }
        />
      ) : (
        <span className="value">{val}</span>
      )}
    </div>
  );
};

/* ================= RULE SELECT ================= */
const RuleSelect = ({
  label,
  name,
  options,
  appId,
  isEditable,
  editData,
  handleEditChange
}) => {

  const val =
    editData?.[appId]?.ruleSpecificDataJson?.[name] ?? "";

  return (
    <div className="detail-item">
      <span className="label">{label}</span>

      {isEditable ? (
        <select
          value={val}
          onChange={(e) =>
            handleEditChange(appId, name, e.target.value)
          }
        >
          <option value="">Select</option>
          {options.map(o => (
            <option key={o} value={o}>{o}</option>
          ))}
        </select>
      ) : (
        <span className="value">{val}</span>
      )}
    </div>
  );
};

/* ================= MAIN COMPONENT ================= */
const RuleDetailsSection = React.memo(({
  appId,
  ruleId,
  jsonData,
  isRowEditable,
  editData,
  handleEditChange
}) => {

  if (!jsonData) return null;

  return (
    <>
      <h4>Rule Specific Details</h4>

      <div className="rule-grid">

        {/* 🏠 HOUSE */}
        {ruleId === 1 && (
          <>
            <RuleInput label="Location & Measurement" name="location" appId={appId} isEditable={isRowEditable} editData={editData} handleEditChange={handleEditChange} />
            <RuleSelect label="Ownership Type" name="ownershipType" options={["Freehold", "Leasehold"]} appId={appId} isEditable={isRowEditable} editData={editData} handleEditChange={handleEditChange} />
            <RuleInput label="Construction Plan" name="constructionPlan" appId={appId} isEditable={isRowEditable} editData={editData} handleEditChange={handleEditChange} />
            <RuleInput label="Society Name" name="societyName" appId={appId} isEditable={isRowEditable} editData={editData} handleEditChange={handleEditChange} />
            <RuleInput label="Cost of Construction" name="constructionCost" type="number" appId={appId} isEditable={isRowEditable} editData={editData} handleEditChange={handleEditChange} />
            <RuleInput label="Housing Board" name="housingBoardName" appId={appId} isEditable={isRowEditable} editData={editData} handleEditChange={handleEditChange} />
          </>
        )}

        {/* 🎓 EDUCATION */}
        {ruleId === 2 && (
          <>
            <RuleInput label="Child Name" name="childName" appId={appId} isEditable={isRowEditable} editData={editData} handleEditChange={handleEditChange} />
            <RuleInput label="Class / College" name="classCollege" appId={appId} isEditable={isRowEditable} editData={editData} handleEditChange={handleEditChange} />
            <RuleSelect label="Student Type" name="studentType" options={["DayScholar", "Hostler"]} appId={appId} isEditable={isRowEditable} editData={editData} handleEditChange={handleEditChange} />
          </>
        )}

        {/* 🏥 MEDICAL */}
        {ruleId === 3 && (
          <>
            <RuleInput label="Patient Details" name="patientDetails" appId={appId} isEditable={isRowEditable} editData={editData} handleEditChange={handleEditChange} />
            <RuleInput label="Hospital / Doctor" name="hospitalName" appId={appId} isEditable={isRowEditable} editData={editData} handleEditChange={handleEditChange} />
            <RuleSelect label="Patient Type" name="patientType" options={["Indoor", "Outdoor"]} appId={appId} isEditable={isRowEditable} editData={editData} handleEditChange={handleEditChange} />
            <RuleSelect label="Reimbursement Available" name="reimbursementAvailable" options={["true", "false"]} appId={appId} isEditable={isRowEditable} editData={editData} handleEditChange={handleEditChange} />
          </>
        )}

      </div>
    </>
  );
});

export default RuleDetailsSection;