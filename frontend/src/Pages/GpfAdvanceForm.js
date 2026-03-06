import React, { useState, useEffect } from "react";
import api from "../api/axios";
import "../styles/GpfAdvanceForm.css";
import {
  getMasterByEmpCode,
  getDetailsByAccount
} from "../mock/gpfMockApi";

const GpfAdvanceForm = () => {

const [empCodeInput,setEmpCodeInput] = useState("");
const [gpfAccountInput,setGpfAccountInput] = useState("");

const [masterApiData,setMasterApiData] = useState(null);
const [detailsApiData,setDetailsApiData] = useState(null);

const [rules,setRules] = useState([]);
const [selectedRuleId,setSelectedRuleId] = useState("");

const [errors,setErrors] = useState({});
const [showVerification,setShowVerification] = useState(false);
const [showSuccess,setShowSuccess] = useState(false);
const getTodayDate = ()=>{
 const today = new Date();
 return today.toISOString().split("T")[0];
};

const [userInput,setUserInput] = useState({

 amountofadvancerequested:"",
 purposeofadvance:"",
 noofmonthlyinstallmentsforpaymentofconsolidatedadvance:"",
 dateofapplication:getTodayDate()

});

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
 setSelectedRuleId("");

 setUserInput({
  amountofadvancerequested:"",
  purposeofadvance:"",
  noofmonthlyinstallmentsforpaymentofconsolidatedadvance:"",
  dateofapplication:getTodayDate()
 });

};

useEffect(() => {

  if (!empCodeInput) return;

  const timer = setTimeout(async () => {

    try {

      const empCode = empCodeInput.trim().toUpperCase();   // ⭐ normalize

      const master = await getMasterByEmpCode(empCode);

      if (master) {
        setMasterApiData(master);
      }

    } catch (err) {
      console.error("Master fetch failed", err);
    }

  }, 400);

  return () => clearTimeout(timer);

}, [empCodeInput]);

useEffect(() => {

  if (!gpfAccountInput) return;

  const timer = setTimeout(async () => {

    try {

      const account = gpfAccountInput.trim().toUpperCase();   // ⭐ normalize

      const details = await getDetailsByAccount(account);

      if (details) {
        setDetailsApiData(details);
      }

    } catch (err) {
      console.error("Details fetch failed", err);
    }

  }, 400);

  return () => clearTimeout(timer);

}, [gpfAccountInput]);

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
 Number(detailsApiData?.amountofadvanceoutstanding || 0);

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

 if(consolidatedAdvance > Number(detailsApiData?.netbalance || 0))
  newErrors.amount="Advance exceeds available balance";

 setErrors(newErrors);

 return Object.keys(newErrors).length === 0;

};

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

    gpfaccountno:gpfAccountInput,

    creditfromdate:getCurrentFinancialYearStartDate(),
    credittodate:getTodayDate(),

    dateofoutstandingbalance:getLastFinancialYearEndDate(),

    withdrawlfromdate:getCurrentFinancialYearStartDate(),
    withdrawltodate:getTodayDate(),

    totalwithdrawlamount:detailsApiData.totalwithdrawlamount,

    amountofadvancerequested:userInput.amountofadvancerequested,
    purposeofadvance:userInput.purposeofadvance,

    noofmonthlyinstallmentsforpaymentofconsolidatedadvance:
      userInput.noofmonthlyinstallmentsforpaymentofconsolidatedadvance,

    advancerule:Number(selectedRuleId),

    dateofapplication:userInput.dateofapplication,

    amountofconsolidatedadvance:consolidatedAdvance
  },

  roleId:28,
  actionId:17

 };

 await api.post("/gpf-advance/save",payload);

 setShowVerification(false);
 setShowSuccess(true);

 resetForm();

 }catch(err){

 console.error(err);
 alert(err.response?.data || "Save failed");

 }

};
/* ================= UI ================= */

return (

<div className="container">

<h2>GPF Advance Application</h2>

<h3>Employee Details</h3>

<div className="form-section">

<div className="form-row">

<div className="form-group">
<label>Employee Code</label>
<input
className="form-input"
value={empCodeInput}
onChange={(e)=>setEmpCodeInput(e.target.value)}
/>
</div>

</div>

<div className="info-grid">

<div className="info-card">
<div className="info-label">Name</div>
<div className="info-value">{masterApiData?.empname || "-"}</div>
</div>

<div className="info-card">
<div className="info-label">Designation</div>
<div className="info-value">{masterApiData?.designation || "-"}</div>
</div>

<div className="info-card">
<div className="info-label">Division</div>
<div className="info-value">{masterApiData?.empdivision || "-"}</div>
</div>

<div className="info-card">
<div className="info-label">Mobile</div>
<div className="info-value">{masterApiData?.empmobileno || "-"}</div>
</div>

<div className="info-card">
<div className="info-label">Email</div>
<div className="info-value">{masterApiData?.empemailid || "-"}</div>
</div>

</div>

</div>

<hr/>

<h3>Advance Request</h3>

<div className="form-section">

<div className="form-row">

<div className="form-group">
<label>GPF Account</label>
<input
className="form-input"
value={gpfAccountInput}
onChange={(e)=>setGpfAccountInput(e.target.value)}
/>
</div>

<div className="form-group">
<label>Advance Rule</label>
<select
className="form-input"
value={selectedRuleId}
onChange={(e)=>setSelectedRuleId(e.target.value)}
>
<option value="">Select Rule</option>
{rules.map(r=>(
<option key={r.ruleId} value={r.ruleId}>
{r.advanceReason}
</option>
))}
</select>
</div>

<div className="form-group">
<label>Advance Requested</label>
<input
className="form-input"
name="amountofadvancerequested"
value={userInput.amountofadvancerequested || ""}
onChange={handleChange}
/>
</div>

<div className="form-group">
<label>No. of Monthly Installments</label>
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
<label>Date of Application</label>
<input
type="date"
className="form-input"
name="dateofapplication"
value={userInput.dateofapplication}
onChange={handleChange}
/>
</div>
</div>
<div className="form-row">


<div className="form-group">
<label>Purpose</label>
<textarea
className="form-input"
name="purposeofadvance"
value={userInput.purposeofadvance || ""}
onChange={handleChange}
/>
</div>
</div>


</div>

<h3>GPF Financial Details</h3>

<div className="info-grid">

<div className="info-card">
<div className="info-label">Basic Pay</div>
<div className="info-value">₹{detailsApiData?.basicpay || "-"}</div>
</div>

<div className="info-card">
<div className="info-label">Outstanding Balance As On</div>
<div className="info-value">
{formatDate(getLastFinancialYearEndDate())}
</div>
</div>

<div className="info-card">
<div className="info-label">Outstanding Balance</div>
<div className="info-value">
₹{detailsApiData?.outstandingbalance || "-"}
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

</div>

<h3>Installment Calculation</h3>

<div className="rule-description-box">
<div>Months : {months}</div>
<div>Monthly Deduction : ₹{monthlyInstallment}</div>
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