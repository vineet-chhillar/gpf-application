import React from "react";
import "../styles/GpfWorkflowPageworkflow.css";

/* ================= RULE INPUT ================= */
const RuleInput = ({
  label,
  name,
  type = "text",
  appId,
  isEditable,
  editData,
  jsonData,
  handleEditChange
}) => {
  const val = isEditable
    ? editData?.[appId]?.ruleSpecificDataJson?.[name]
        ?? jsonData?.[name]
        ?? ""
    : jsonData?.[name] ?? "";

  // Keep the remaining JSX unchanged



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
  jsonData,
  handleEditChange
}) => {
  const val = isEditable
    ? editData?.[appId]?.ruleSpecificDataJson?.[name]
        ?? jsonData?.[name]
        ?? ""
    : jsonData?.[name] ?? "";

  // Keep the remaining JSX unchanged

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

  const normalizedRuleId = Number(ruleId);

  console.log("Rule Details Debug:", {
    appId,
    ruleId,
    normalizedRuleId,
    jsonData,
    isRowEditable
  });

  if (!jsonData) {
    console.log("Rule specific data is missing for appId:", appId);
    return null;
  }

  return (
    <>
      <h4>Rule Specific Details</h4>

      <div className="rule-grid">

        
        
{normalizedRuleId === 1 && (
  <>
    <RuleInput
      label="Location & Measurement"
      name="location"
      appId={appId}
      isEditable={isRowEditable}
      editData={editData}
      jsonData={jsonData}
      handleEditChange={handleEditChange}
    />

    <RuleSelect
      label="Ownership Type"
      name="ownershipType"
      options={["Freehold", "Leasehold"]}
      appId={appId}
      isEditable={isRowEditable}
      editData={editData}
      jsonData={jsonData}
      handleEditChange={handleEditChange}
    />

    <RuleInput
      label="Construction Plan"
      name="constructionPlan"
      appId={appId}
      isEditable={isRowEditable}
      editData={editData}
      jsonData={jsonData}
      handleEditChange={handleEditChange}
    />

    <RuleInput
      label="Society Name"
      name="societyName"
      appId={appId}
      isEditable={isRowEditable}
      editData={editData}
      jsonData={jsonData}
      handleEditChange={handleEditChange}
    />

    <RuleInput
      label="Cost of Construction"
      name="constructionCost"
      type="number"
      appId={appId}
      isEditable={isRowEditable}
      editData={editData}
      jsonData={jsonData}
      handleEditChange={handleEditChange}
    />

    <RuleInput
      label="Housing Board"
      name="housingBoardName"
      appId={appId}
      isEditable={isRowEditable}
      editData={editData}
      jsonData={jsonData}
      handleEditChange={handleEditChange}
    />
  </>
)}


        {/* 🎓 EDUCATION */}
        
        {normalizedRuleId === 2 && (
  <>
    <RuleInput
      label="Child Name"
      name="childName"
      appId={appId}
      isEditable={isRowEditable}
      editData={editData}
      jsonData={jsonData}
      handleEditChange={handleEditChange}
    />

    <RuleInput
      label="Class / College"
      name="classCollege"
      appId={appId}
      isEditable={isRowEditable}
      editData={editData}
      jsonData={jsonData}
      handleEditChange={handleEditChange}
    />

    <RuleSelect
      label="Student Type"
      name="studentType"
      options={["DayScholar", "Hostler"]}
      appId={appId}
      isEditable={isRowEditable}
      editData={editData}
      jsonData={jsonData}
      handleEditChange={handleEditChange}
    />
  </>
)}

        {/* 🏥 MEDICAL */}
        
{normalizedRuleId === 3 && (
  <>
    <RuleInput
      label="Patient Details"
      name="patientDetails"
      appId={appId}
      isEditable={isRowEditable}
      editData={editData}
      jsonData={jsonData}
      handleEditChange={handleEditChange}
    />

    <RuleInput
      label="Hospital / Doctor"
      name="hospitalName"
      appId={appId}
      isEditable={isRowEditable}
      editData={editData}
      jsonData={jsonData}
      handleEditChange={handleEditChange}
    />

    <RuleSelect
      label="Patient Type"
      name="patientType"
      options={["Indoor", "Outdoor"]}
      appId={appId}
      isEditable={isRowEditable}
      editData={editData}
      jsonData={jsonData}
      handleEditChange={handleEditChange}
    />

    <RuleSelect
      label="Reimbursement Available"
      name="reimbursementAvailable"
      options={["true", "false"]}
      appId={appId}
      isEditable={isRowEditable}
      editData={editData}
      jsonData={jsonData}
      handleEditChange={handleEditChange}
    />
  </>
)}


      </div>
    </>
  );
});

export default RuleDetailsSection;