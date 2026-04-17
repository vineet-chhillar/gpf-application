import React, { useState, useEffect } from "react";
import api from "../api/axios";
import { useRef } from "react";
import "../styles/GpfForm.css";
import {
  getMasterByEmpCode,
  getDetailsByPan
} from "../mock/gpfMockApi";

const GpfAdvanceForm = () => {

 

  const dropdownRef = useRef(null);
const [empCodeInput,setEmpCodeInput] = useState("");
const [gpfAccountInput,setGpfAccountInput] = useState("");

const [masterApiData,setMasterApiData] = useState(null);
const [detailsApiData,setDetailsApiData] = useState(null);

const [rules,setRules] = useState([]);
const [selectedRuleId,setSelectedRuleId] = useState(null);

const [errors,setErrors] = useState({});
const [showVerification,setShowVerification] = useState(false);
const [showSuccess,setShowSuccess] = useState(false);
const getTodayDate = ()=>{
 const today = new Date();
 return today.toISOString().split("T")[0];
};
const [ruleSpecificData, setRuleSpecificData] = useState({});
const [userInput,setUserInput] = useState({
 amountofadvancerequested:"",
 purposeofadvance:"",
 noofmonthlyinstallmentsforpaymentofconsolidatedadvance:"",
 particulars:"",  
 dateofapplication:getTodayDate()
});
const handleRuleDataChange = (e) => {
  const { name, value } = e.target;

  setRuleSpecificData(prev => ({
    ...prev,
    [name]: value
  }));
};
const [dropdownOpen, setDropdownOpen] = useState(false);
const [hoveredRule, setHoveredRule] = useState(null);

const getLastFinancialYearEndDate = () => {

  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth();

  const fyEndYear = month < 3 ? year - 1 : year;

  return `${fyEndYear}-03-31`;
};

const getCurrentFinancialYearStartDate = () => {

  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth();

  const fyStartYear = month < 3 ? year - 1 : year;

  return `${fyStartYear}-04-01`;
};

const formatDate = (dateString) => {

  if (!dateString) return "";

  const date = new Date(dateString);

  const day = String(date.getDate()).padStart(2, "0");
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const year = date.getFullYear();

  return `${day}/${month}/${year}`;
};

const resetForm = ()=>{

 setMasterApiData(null);
 setDetailsApiData(null);
 setEmpCodeInput("");
 setGpfAccountInput("");
 setSelectedRuleId(null);
setRuleSpecificData({});
 setUserInput({
  amountofadvancerequested:"",
  purposeofadvance:"",
  noofmonthlyinstallmentsforpaymentofconsolidatedadvance:"",
  dateofapplication:getTodayDate(),
  particulars:"",  
 });

};

const handleEmpCodeBlur = async () => {

  if (!empCodeInput) {
    setMasterApiData(null);
    setDetailsApiData(null);
    setGpfAccountInput("");
    return;
  }

  try {
    const empCode = empCodeInput.trim().toUpperCase();

    const master = await getMasterByEmpCode(empCode);

    if (!master) {
      alert("Employee not found");
      return;
    }

    setMasterApiData(master);

  } catch (err) {
    console.error("Master fetch failed", err);
    setMasterApiData(null);
    setDetailsApiData(null);
    setGpfAccountInput("");
  }
};

useEffect(() => {

  if (!masterApiData?.panno) {
    setDetailsApiData(null); 
    setGpfAccountInput("");
    return;
  }

  const timer = setTimeout(async () => {

    try {

      const pan = masterApiData.panno.trim().toUpperCase();

      const details = await getDetailsByPan(pan);

      if (details) {
  setDetailsApiData(details);
  setGpfAccountInput(details.gpfaccountno || ""); // ✅ NEW
} else {
  setDetailsApiData(null);
  setGpfAccountInput(""); // ✅ clear
}

    } catch (err) {

      console.error("Details fetch failed", err);
      setDetailsApiData(null); // ❗ API error

    }

  }, 400);

  return () => clearTimeout(timer);

}, [masterApiData]);

{/*useEffect(()=>{

 if(!empCodeInput) return;

 setGpfAccountInput(`GPF-NIC-${empCodeInput}`);

},[empCodeInput]);*/}

useEffect(()=>{

 api.get("/gpf/advance-rules/active")
 .then(res=>setRules(res.data));

},[]);

const handleChange = (e)=>{

 const {name,value} = e.target;

 setUserInput(prev=>({
  ...prev,
  [name]:value
 }));

};

const handleUserChange = (e) => {

 const { name, value } = e.target;

 let newValue = value;

 if(name === "noofmonthlyinstallmentsforpaymentofconsolidatedadvance"){

  if(value > 60) newValue = 60;
  if(value < 1) newValue = 1;

 }

 setUserInput(prev => ({
  ...prev,
  [name]: newValue
 }));

};

/* ================= CALCULATIONS ================= */

const advanceOutstanding =
 Number(detailsApiData?.outstandingbalance ?? 0);

const requested =
 Number(userInput.amountofadvancerequested || 0);

const consolidatedAdvance =
 advanceOutstanding + requested;

const months =
 Number(userInput.noofmonthlyinstallmentsforpaymentofconsolidatedadvance || 0);

const monthlyInstallment =
 months ? Math.ceil(consolidatedAdvance / months) : 0;

/* ================= VALIDATION ================= */

const validate = ()=>{

 const newErrors = {};

 if(!requested)
  newErrors.amount="Enter advance amount";

 if(!userInput.purposeofadvance)
  newErrors.purpose="Purpose required";

 if(!selectedRuleId)
  newErrors.rule="Rule required";

 if(!months)
  newErrors.installments="Installments required";

 if(months > 60)
  newErrors.installments="Maximum 60 installments allowed";

const netBalance = Number(detailsApiData?.netbalance ?? 0);

if (consolidatedAdvance > netBalance) {
  newErrors.amount = "Advance exceeds available balance";
}

 if (!userInput.particulars || !userInput.particulars.trim()) {
  newErrors.particulars = "Particulars is required";
}
if (selectedRuleId == 1) {

  if (!ruleSpecificData.location)
    newErrors.location = "Location is required";

  if (!ruleSpecificData.ownershipType)
    newErrors.ownershipType = "Ownership type required";

  if (!ruleSpecificData.constructionPlan)
    newErrors.constructionPlan = "Construction plan required";

  if (!ruleSpecificData.constructionCost)
    newErrors.constructionCost = "Cost required";

}
if (selectedRuleId == 2) {

  if (!ruleSpecificData.childName)
    newErrors.childName = "Child name required";

  if (!ruleSpecificData.classCollege)
    newErrors.classCollege = "Class/College required";

  if (!ruleSpecificData.studentType)
    newErrors.studentType = "Student type required";

}

if (selectedRuleId == 3) {

  if (!ruleSpecificData.patientDetails)
    newErrors.patientDetails = "Patient details required";

  if (!ruleSpecificData.hospitalName)
    newErrors.hospitalName = "Hospital/Doctor required";

  if (!ruleSpecificData.patientType)
    newErrors.patientType = "Patient type required";

}
if (ruleSpecificData.constructionCost && isNaN(ruleSpecificData.constructionCost)) {
  newErrors.constructionCost = "Must be a number";
}
 setErrors(newErrors);

 return Object.keys(newErrors).length === 0;

};

useEffect(() => {
  const handleClickOutside = (event) => {
    if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
      setDropdownOpen(false);
      setHoveredRule(null);   // 🔥 ADD THIS
    }
  };

  document.addEventListener("mousedown", handleClickOutside);

  return () => {
    document.removeEventListener("mousedown", handleClickOutside);
  };
}, []);
useEffect(() => {
  if (rules.length && selectedRuleId) {
    const exists = rules.find(r => r.ruleId === selectedRuleId);
    if (!exists) {
      setSelectedRuleId(null);
    }
  }
}, [rules]);
/* ================= SUBMIT ================= */

const handleSubmit = () => {

 if(!validate()) return;

 if(!masterApiData || !detailsApiData){
  alert("Required data not loaded");
  return;
 }

 setShowVerification(true);

};
const confirmSubmit = async () => {

 try{

 const payload = {

  master:{
    ...masterApiData
    
  },

  details:{
    ...detailsApiData,

  particulars: userInput.particulars,   

  
    gpfaccountno: detailsApiData?.gpfaccountno,
panno:masterApiData?.panno,

    creditfromdate:getCurrentFinancialYearStartDate(),
    credittodate:getTodayDate(),

    dateofoutstandingbalance:getLastFinancialYearEndDate(),

    withdrawlfromdate:getCurrentFinancialYearStartDate(),
    withdrawltodate:getTodayDate(),

    totalwithdrawlamount: detailsApiData?.totalwithdrawlamount ?? 0,

    amountofadvancerequested:userInput.amountofadvancerequested,
    purposeofadvance:userInput.purposeofadvance,

    noofmonthlyinstallmentsforpaymentofconsolidatedadvance:
      userInput.noofmonthlyinstallmentsforpaymentofconsolidatedadvance,

    advancerule:Number(selectedRuleId),

    dateofapplication:userInput.dateofapplication,

      closingbalance: detailsApiData?.closingbalance || 0,
    amountofadvanceoutstanding: detailsApiData?.outstandingbalance || 0,
    amountofconsolidatedadvance:consolidatedAdvance
  },
ruleSpecificData: ruleSpecificData,   
  roleId:28,
  actionId:17

 };

 await api.post("/gpf-advance/save",payload);

 setShowVerification(false);
 setShowSuccess(true);

 resetForm();

 }catch (err) {
  console.error(err);

  const message =
    err.response?.data?.message ||   // ✅ if backend sends JSON
    err.response?.data ||            // ✅ if plain string
    err.message ||                   // fallback
    "Save failed";

  alert(message);
}

};

/* ================= UI ================= */
const selectedRule = rules.find(
  r => r.ruleId === selectedRuleId
);

const selectedRuleText =
  selectedRuleId === null
    ? "Select Rule"
    : selectedRule?.advanceReason || "Select Rule";

console.log("Selected:", selectedRuleId, selectedRule);
console.log("Rules:", rules);
return (

<div className="container">

{/*<h2>GPF Advance Application</h2>*/}

<h3>Employee Details</h3>

<div className="form-section">

<div className="form-row">

<div className="form-group">
<label className="form-label">Employee Code <span className="required">*</span></label>
<input
  className="form-input"
  value={empCodeInput}
  onChange={(e) => {
    const value = e.target.value;
    setEmpCodeInput(value);

    // 🔥 instant clear (prevents flicker of old data)
    setMasterApiData(null);
    setDetailsApiData(null);
    setGpfAccountInput("");
  }}
  onBlur={handleEmpCodeBlur}
  onKeyDown={(e) => {
    if (e.key === "Enter") handleEmpCodeBlur();
  }}
/>
</div>

</div>

<div className="info-grid">
{false && (
<div className="info-card">
<div className="info-label">Employee Code</div>
<div className="info-value">{masterApiData?.empcode || "-"}</div>
</div>
)}

<div className="info-card">
<div className="info-label">Employee Name</div>
<div className="info-value">{masterApiData?.empname || "-"}</div>
</div>

<div className="info-card">
<div className="info-label">Designation</div>
<div className="info-value">{masterApiData?.designation || "-"}</div>
</div>

{false && (
<div className="info-card">
<div className="info-label">Division</div>
<div className="info-value">{masterApiData?.empdivision || "-"}</div>
</div>
)}

<div className="info-card">
<div className="info-label">Mobile</div>
<div className="info-value">{masterApiData?.empmobileno || "-"}</div>
</div>

<div className="info-card">
<div className="info-label">Email</div>
<div className="info-value">{masterApiData?.empemailid || "-"}</div>
</div>


<div className="info-card">
<div className="info-label">Date of Joining</div>
<div className="info-value">{formatDate(masterApiData?.dateofjoining)}</div>
</div>

<div className="info-card">
<div className="info-label">Retirement Date</div>
<div className="info-value">{formatDate(masterApiData?.dateofsuperannuation)}</div>
</div>

<div className="info-card">
<div className="info-label">PAN No</div>
<div className="info-value">{masterApiData?.panno || "-"}</div>
</div>

<div className="info-card">
<div className="info-label">GPF Account No</div>
<div className="info-value">{gpfAccountInput}</div>
</div>

<div className="info-card">
<div className="info-label">Date of Application</div>
<div className="info-value">{formatDate(userInput.dateofapplication)}</div>
</div>

{/*<div className="form-group">
<label className="form-label">Date of Application</label>
<input
type="date"
className="form-input form-input-sm"
name="dateofapplication"
value={userInput.dateofapplication}
onChange={handleChange}
readOnly
/>
</div>*/}

{/*
<div className="form-group">
<label className="form-label">GPF Account</label>
<input
className="form-input"
value={gpfAccountInput}
readOnly
/>
</div>
*/}

</div>

</div>

<hr/>

<h3>Advance Rule Details</h3>

<div className="form-section">

  {/* ===== RULE DROPDOWN ===== */}
  <div className="form-row-compact">

    <div className="form-group ">
  <label className="form-label">Advance Rule<span className="required">*</span></label>

  <div className="custom-dropdown" ref={dropdownRef}>

    {/* Selected value */}
    <div
  className="dropdown-selected"
  onClick={() => setDropdownOpen(!dropdownOpen)}
  
>
  {selectedRuleId === null
    ? "Select Rule"
    : selectedRule
      ? selectedRule.advanceReason
      : "Select Rule"}
</div>

    {/* Dropdown menu */}
    {dropdownOpen && (
      <div className="dropdown-menu">
        {rules.map(rule => (
          <div
            key={rule.ruleId}
            className="dropdown-item"
            onClick={() => {
               setSelectedRuleId(rule.ruleId ?? null);
               setRuleSpecificData({});
              setDropdownOpen(false);
              setHoveredRule(null);   // 🔥 ADD THIS
            }}
            onMouseEnter={() => setHoveredRule(rule)}
            onMouseLeave={() => setHoveredRule(null)}
          >
            {rule.advanceReason}
          </div>
        ))}
      </div>
    )}

    {/* 🔥 Hover modal (above dropdown) */}
    {hoveredRule && (
      <div className="rule-hover-modal">
        <strong>{hoveredRule.advanceReason}</strong>
        <p>{hoveredRule.ruleDescription}</p>
      </div>
    )}

  
</div>
</div>

  </div>
  <div className="form-row-compact">
{/* ================= RULE BASED FIELDS ================= */}

{selectedRuleId === 1 && (  // 🏠 HOUSE BUILDING
  <>
    <div className="form-group">
      <label className="form-label">Location & Measurement<span className="required">*</span></label>
      <input name="location" className="form-input" onChange={handleRuleDataChange} />
    </div>

    <div className="form-group">
      <label className="form-label">Ownership Type<span className="required">*</span></label>
      <select name="ownershipType" className="form-input" onChange={handleRuleDataChange}>
        <option value="">Select<span className="required">*</span></option>
        <option value="Freehold">Freehold</option>
        <option value="Leasehold">Leasehold</option>
      </select>
    </div>

    <div className="form-group">
      <label className="form-label">Construction Plan<span className="required">*</span></label>
      <input name="constructionPlan" className="form-input" onChange={handleRuleDataChange} />
    </div>

    <div className="form-group">
      <label className="form-label">Society Name<span className="required">*</span></label>
      <input name="societyName" className="form-input" onChange={handleRuleDataChange} />
    </div>

    <div className="form-group sm">
      <label className="form-label">Cost of Construction<span className="required">*</span></label>
      <input name="constructionCost" className="form-input form-input-sm" onChange={handleRuleDataChange} />
    </div>

    <div className="form-group">
      <label className="form-label">Housing Board<span className="required">*</span></label>
      <input name="housingBoardName" className="form-input" onChange={handleRuleDataChange} />
    </div>
  </>
)}

{selectedRuleId === 2 && (  // 🎓 EDUCATION
  <>
    <div className="form-group">
      <label className="form-label">Child Name<span className="required">*</span></label>
      <input name="childName" className="form-input" onChange={handleRuleDataChange} />
    </div>

    <div className="form-group">
      <label className="form-label">Class / College<span className="required">*</span></label>
      <input name="classCollege" className="form-input" onChange={handleRuleDataChange} />
    </div>

    <div className="form-group">
      <label className="form-label">Student Type<span className="required">*</span></label>
      <select name="studentType" className="form-input" onChange={handleRuleDataChange}>
        <option value="">Select<span className="required">*</span></option>
        <option value="DayScholar">Day Scholar</option>
        <option value="Hostler">Hostler</option>
      </select>
    </div>
  </>
)}

{selectedRuleId === 3 && (  // 🏥 MEDICAL
  <>
   <div className="form-group">
  <label className="form-label">Patient Name & Relationship<span className="required">*</span></label>
  <input
    name="patientDetails"
    className="form-input"
    placeholder="e.g. Rahul (Son)"
    onChange={handleRuleDataChange}
  />
</div>

    <div className="form-group">
      <label className="form-label">Hospital / Doctor<span className="required">*</span></label>
      <input name="hospitalName" className="form-input" onChange={handleRuleDataChange} />
    </div>

    <div className="form-group">
      <label className="form-label">Patient Type<span className="required">*</span></label>
      <select name="patientType" className="form-input" onChange={handleRuleDataChange}>
        <option value="">Select<span className="required">*</span></option>
        <option value="Indoor">Indoor</option>
        <option value="Outdoor">Outdoor</option>
      </select>
    </div>

    <div className="form-group">
      <label className="form-label">Reimbursement Available<span className="required">*</span></label>
      <select name="reimbursementAvailable" className="form-input" onChange={handleRuleDataChange}>
        <option value="">Select<span className="required">*</span></option>
        <option value="true">Yes</option>
        <option value="false">No</option>
      </select>
    </div>
    
    </>
)}
</div>
  </div>

<h3>Advance Request</h3>

<div className="form-section">

<div className="form-row-compact">






<div className="form-group">
<label className="form-label">Amount of Advance Required<span className="required">*</span></label>
<input
className="form-input"
name="amountofadvancerequested"
value={userInput.amountofadvancerequested || ""}
onChange={handleChange}
/>
</div>

<div className="form-group">
<label className="form-label">No. of Monthly Installments<span className="required">*</span></label>
<input
type="number"
className="form-input"
name="noofmonthlyinstallmentsforpaymentofconsolidatedadvance"
value={userInput.noofmonthlyinstallmentsforpaymentofconsolidatedadvance || ""}
onChange={handleUserChange}
min="1"
max="60"
/>
</div>



<div className="form-group">
<label className="form-label">Purpose<span className="required">*</span></label>
<textarea
className="form-input"
name="purposeofadvance"
value={userInput.purposeofadvance || ""}
onChange={handleChange}
/>
</div>


<div className="form-group">
  <label className="form-label">Particulars of Pecuniary Circumstances<span className="required">*</span></label>
  <input
    className="form-input"
    name="particulars"
    value={userInput.particulars || ""}
    onChange={handleChange}
  />
</div>

</div>











<div className="info-grid">



<div className="info-card">
<div className="info-label">Basic Pay</div>
<div className="info-value">₹{detailsApiData?.basicpay || "-"}</div>
</div>

<div className="info-card">
<div className="info-label">Closing Balance As On</div>
<div className="info-value">
{formatDate(getLastFinancialYearEndDate())}
</div>
</div>

<div className="info-card">
<div className="info-label">Closing Balance</div>
<div className="info-value">
₹{detailsApiData?.closingbalance || "-"}
</div>
</div>

<div className="info-card">
<div className="info-label">Credit From</div>
<div className="info-value">
{formatDate(getCurrentFinancialYearStartDate())}
</div>
</div>

<div className="info-card">
<div className="info-label">Credit To</div>
<div className="info-value">
{formatDate(getTodayDate())}
</div>
</div>

<div className="info-card">
<div className="info-label">Total Credit Amount</div>
<div className="info-value">
₹{detailsApiData?.totalcreditamount || "-"}
</div>
</div>

<div className="info-card">
<div className="info-label">Withdraw From</div>
<div className="info-value">
{formatDate(getCurrentFinancialYearStartDate())}
</div>
</div>

<div className="info-card">
<div className="info-label">Withdraw To</div>
<div className="info-value">
{formatDate(getTodayDate())}
</div>
</div>

<div className="info-card">
<div className="info-label">Total Withdrawal Amount</div>
<div className="info-value">
₹{detailsApiData?.totalwithdrawlamount || "-"}
</div>
</div>

<div className="info-card">
<div className="info-label">Refund After Outstanding</div>
<div className="info-value">
₹{detailsApiData?.refundafterdateofoutstandingbalance || "-"}
</div>
</div>

{/*<div className="info-card">
<div className="info-label">Advance Outstanding</div>
<div className="info-value">
₹{detailsApiData?.closingbalance || "-"}
</div>
</div>*/}



</div>
</div>
<h3>Advance Liability</h3>

<div className="info-grid">

<div className="info-card">
<div className="info-label">Advance Outstanding</div>
<div className="info-value">₹{advanceOutstanding}</div>
</div>

<div className="info-card">
<div className="info-label">Consolidated Advance</div>
<div className="info-value">₹{consolidatedAdvance}</div>
</div>



<div className="rule-description-box">
  
<div>Installment || Months : {months} Monthly Deduction : ₹{monthlyInstallment}</div>

</div>



</div>
{Object.keys(errors).length > 0 && (

<div className="error-summary">
<ul>
{Object.values(errors).map((e,i)=>(<li key={i}>{e}</li>))}
</ul>
</div>

)}


<div className="form-row-center">

{showVerification && (

<div className="modal-overlay">

<div className="modal-box">

<h3>Verify Advance Application</h3>

<div className="modal-content">

<div className="modal-row">
<span>Advance Outstanding</span>
<strong>₹{advanceOutstanding}</strong>
</div>

<div className="modal-row">
<span>Advance Requested</span>
<strong>₹{requested}</strong>
</div>

<div className="modal-row highlight">
<span>Consolidated Advance</span>
<strong>₹{consolidatedAdvance}</strong>
</div>

<div className="modal-row">
<span>Monthly Installment</span>
<strong>₹{monthlyInstallment}</strong>
</div>

</div>

<div className="modal-actions">

<button
className="confirm-btn"
onClick={confirmSubmit}
>
Confirm
</button>

<button
className="cancel-btn"
onClick={()=>setShowVerification(false)}
>
Cancel
</button>

</div>

</div>

</div>

)}

<button className="process-btn" onClick={handleSubmit}>
Submit Advance Application
</button>


{showSuccess && (

<div className="modal-overlay">

<div className="modal-box success">

<h3>✅ Advance Submitted Successfully</h3>

<p>Your GPF advance application has been submitted.</p>

<button
className="confirm-btn"
onClick={()=>setShowSuccess(false)}
>
OK
</button>

</div>

</div>

)}
</div>

</div>

);

};

export default GpfAdvanceForm;