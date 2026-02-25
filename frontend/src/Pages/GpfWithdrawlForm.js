import React, { useState, useEffect } from "react";
import api from "../api/axios";
import "../styles/GpfWithdrawlForm.css";
import { useRef } from "react";
import {
  getMasterByEmpCode,
  getDetailsByAccount
} from "../mock/gpfMockApi";

//api.get("/gpf/withdrawal-rules/active")


const GpfWithdrawlForm = () => {
const [empCodeInput, setEmpCodeInput] = useState("");
const [gpfAccountInput, setGpfAccountInput] = useState("");

  /* ================= API DATA ================= */
  const [masterApiData, setMasterApiData] = useState(null);
  const [detailsApiData, setDetailsApiData] = useState(null);

  const [showVerification, setShowVerification] = useState(false);

  const formatDate = (dateString) => {
  if (!dateString) return "";

  const date = new Date(dateString);

  const day = String(date.getDate()).padStart(2, "0");
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const year = date.getFullYear();

  return `${day}/${month}/${year}`;
};

  /* ================= USER INPUT ================= */
  const getTodayDate = () => {
  const today = new Date();
  return today.toISOString().split("T")[0]; // yyyy-MM-dd
};
const [rules, setRules] = React.useState([]);
const [selectedRuleId, setSelectedRuleId] = React.useState("");
const selectedRule = rules.find(
  r => r.ruleId === Number(selectedRuleId)
);

const [errors, setErrors] = useState({});

    const [userInput, setUserInput] = useState({
    concernedofficername: "",
    amountofwithdrawlrequested: "",
    purposeofwithdrawl: "",
    ispriorwithdrawlforsamepurpose: false,
    priorwithdrawlamount: "",
    priorwithdrawlfinyear: "",
    dateofapplication: getTodayDate()
  });
  
  const getLastFinancialYearEndDate = () => {
  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth(); // 0 = Jan

  // If before April, FY belongs to previous year
  const fyEndYear = month < 3 ? year - 1 : year;

  return `${fyEndYear}-03-31`; // yyyy-MM-dd
};
const getCurrentFinancialYearStartDate = () => {
  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth(); // 0 = Jan

  // If Jan–Mar → FY started last year
  const fyStartYear = month < 3 ? year - 1 : year;

  return `${fyStartYear}-04-01`; // yyyy-MM-dd
};
const [withdrawalRules, setWithdrawalRules] = useState([]);

const didLoadRef = useRef(false);
//const CURRENT_ROLE = "DDO"; // TEMP – will come from parent app later
{/*const DEFAULT_MASTER_DATA = {
  empcode: "EMP021",
  empname: "Test Employee21",
  designation: "Scientist-B",
  empdivision: "Accounts",
  functionalpost: 11,
  empmobileno: "9876543210",
  empemailid: "test.employee@gov.in",
  dateofjoining: "2019-04-01",
  dateofsuperannuation: "2038-03-31"
};
const DEFAULT_DETAILS_DATA = {
  basicpay: 45000,
  outstandingbalance: 500000,
  totalcreditamount: 50000,
  refundafterdateofoutstandingbalance: 200000,
  totalwithdrawlamount: 100000,
  netbalance: 650000};*/}
useEffect(() => {

  if (!empCodeInput) return;

  const timer = setTimeout(async () => {

    try {

      const master = await getMasterByEmpCode(empCodeInput);

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

      const details = await getDetailsByAccount(gpfAccountInput);

      if (details) {
        setDetailsApiData(details);
      }

    } catch (err) {
      console.error("Details fetch failed", err);
    }

  }, 400);

  return () => clearTimeout(timer);

}, [gpfAccountInput]);
useEffect(() => {

  if (!empCodeInput) return;

  setGpfAccountInput(`GPF-NIC-${empCodeInput}`);

}, [empCodeInput]);
React.useEffect(() => {
  api
    .get("/gpf/withdrawal-rules/active")
    .then(res => {
      setRules(res.data);
    })
    .catch(err => {
      console.error(
        "Failed to load withdrawal rules",
        err.response?.status,
        err.response?.data
      );
    });
}, []);


  /* ================= LOAD DATA ================= */
  useEffect(() => {
  if (didLoadRef.current) return;
  didLoadRef.current = true;

const loadData = async () => {

  try {

    if (!empCodeInput) {
      alert("Enter Employee Code");
      return;
    }

    if (!gpfAccountInput) {
      alert("Enter GPF Account No");
      return;
    }

    const master = await getMasterByEmpCode(empCodeInput);

    const details = await getDetailsByAccount(gpfAccountInput);

    if (!master) {
      alert("Employee not found");
      return;
    }

    if (!details) {
      alert("GPF account not found");
      return;
    }

    setMasterApiData(master);
    setDetailsApiData(details);

  } catch (err) {
    console.error(err);
    alert("Failed to load employee data");
  }

};

  //loadData();
}, []);

  /* ================= HANDLERS ================= */
  const handleUserChange = (e) => {
    const { name, value, type, checked } = e.target;

    setUserInput(prev => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value
    }));
  };

  const buildGpfAccountNo = (empcode) => `GPF-NIC-${empcode}`;
const ONLY_CHARS_REGEX = /^[A-Za-z ]+$/;
const validateForm = () => {
  const newErrors = {};

  // ---- MASTER ----
  {/*if (!userInput.concernedofficername.trim()) {
  newErrors.concernedofficername = "Concerned Officer is required";
} else if (userInput.concernedofficername.length > 100) {
  newErrors.concernedofficername =
    "Concerned Officer name cannot exceed 100 characters";
} else if (!ONLY_CHARS_REGEX.test(userInput.concernedofficername)) {
  newErrors.concernedofficername =
    "Concerned Officer name must contain only alphabets and spaces";
}*/}


  // ---- DETAILS (USER INPUT) ----
  const amount = Number(userInput.amountofwithdrawlrequested);

  if (!amount || amount <= 0) {
    newErrors.amountofwithdrawlrequested =
      "Withdrawal amount must be greater than 0";
  } else if (amount > Number(detailsApiData.netbalance)) {
    newErrors.amountofwithdrawlrequested =
      "Withdrawal amount cannot exceed net balance";
  }

  if (!userInput.purposeofwithdrawl.trim()) {
    newErrors.purposeofwithdrawl = "Purpose of withdrawal is required";
  } else if (userInput.purposeofwithdrawl.length < 5) {
    newErrors.purposeofwithdrawl =
      "Purpose must be at least 5 characters";
  }

  if (!selectedRuleId) {
  newErrors.withdrawlrule = "Withdrawal rule is required";
}


  // ---- PRIOR WITHDRAWAL ----
  if (userInput.ispriorwithdrawlforsamepurpose) {
    if (
      userInput.priorwithdrawlamount === "" ||
      Number(userInput.priorwithdrawlamount) < 0
    ) {
      newErrors.priorwithdrawlamount =
        "Valid prior withdrawal amount required";
    }

    if (!/^\d{4}-\d{2}$/.test(userInput.priorwithdrawlfinyear)) {
      newErrors.priorwithdrawlfinyear =
        "Financial year must be in YYYY-YY format";
    }
  }

  // ---- DATE OF APPLICATION ----
  if (!userInput.dateofapplication) {
    newErrors.dateofapplication = "Date of application is required";
  } else if (userInput.dateofapplication > getTodayDate()) {
    newErrors.dateofapplication =
      "Date of application cannot be in the future";
  }

  setErrors(newErrors);
  return Object.keys(newErrors).length === 0;
};

const confirmSubmit = async () => {

  try {

    const payload = {
      master: {
        ...masterApiData,
        concernedofficername: userInput.concernedofficername
      },
      details: {
        ...detailsApiData,
        gpfaccountno: buildGpfAccountNo(masterApiData.empcode),

        creditfromdate: getCurrentFinancialYearStartDate(),
        credittodate: getTodayDate(),
        dateofoutstandingbalance: getCurrentFinancialYearStartDate(),

        withdrawlfromdate: getCurrentFinancialYearStartDate(),
        withdrawltodate: getTodayDate(),

        amountofwithdrawlrequested: userInput.amountofwithdrawlrequested,
        purposeofwithdrawl: userInput.purposeofwithdrawl,
        withdrawlrule: Number(selectedRuleId),

        ispriorwithdrawlforsamepurpose: userInput.ispriorwithdrawlforsamepurpose,

        priorwithdrawlamount: userInput.ispriorwithdrawlforsamepurpose
          ? userInput.priorwithdrawlamount
          : 0,

        priorwithdrawlfinyear: userInput.ispriorwithdrawlforsamepurpose
          ? userInput.priorwithdrawlfinyear
          : "",

        dateofapplication: userInput.dateofapplication,

        action: { actionId: 17 },
        roleId: 2
      }
    };

    await api.post("/gpf-withdrawl/save", payload);

    alert("✅ Application Submitted Successfully");
    // Clear form
setMasterApiData(null);
setDetailsApiData(null);

setEmpCodeInput("");
setGpfAccountInput("");

setSelectedRuleId("");

setUserInput({
  concernedofficername: "",
  amountofwithdrawlrequested: "",
  purposeofwithdrawl: "",
  ispriorwithdrawlforsamepurpose: false,
  priorwithdrawlamount: "",
  priorwithdrawlfinyear: "",
  dateofapplication: getTodayDate()
});

    setShowVerification(false);

  } catch (err) {
    alert(err.response?.data || "Submission failed");
  }
};

  const handleSubmit = async () => {

  if (!validateForm()) return;

  const withdrawAmount = Number(userInput.amountofwithdrawlrequested);
  const netBalance = Number(detailsApiData.netbalance);

  const confirmMessage = `
Please verify before submitting

Net Balance : ₹${netBalance}
Withdrawal Requested : ₹${withdrawAmount}
Net Balance After Withdrawal : ₹${netBalance - withdrawAmount}
Do you want to continue?
`;

  const confirmed = window.confirm(confirmMessage);

  if (!confirmed) return;

  try {

    if (!masterApiData || !detailsApiData) {
      alert("Required data not loaded");
      return;
    }

    const payload = {
      master: {
        ...masterApiData,
        concernedofficername: userInput.concernedofficername
      },
      details: {
        ...detailsApiData,

        gpfaccountno: buildGpfAccountNo(masterApiData.empcode),

        creditfromdate: getCurrentFinancialYearStartDate(),
        credittodate: getTodayDate(),
        dateofoutstandingbalance: getCurrentFinancialYearStartDate(),

        withdrawlfromdate: getCurrentFinancialYearStartDate(),
        withdrawltodate: getTodayDate(),

        amountofwithdrawlrequested: userInput.amountofwithdrawlrequested,
        purposeofwithdrawl: userInput.purposeofwithdrawl,
        withdrawlrule: Number(selectedRuleId),

        ispriorwithdrawlforsamepurpose:
          userInput.ispriorwithdrawlforsamepurpose,

        priorwithdrawlamount:
          userInput.ispriorwithdrawlforsamepurpose
            ? userInput.priorwithdrawlamount
            : 0,

        priorwithdrawlfinyear:
          userInput.ispriorwithdrawlforsamepurpose
            ? userInput.priorwithdrawlfinyear
            : "",

        dateofapplication: userInput.dateofapplication,

        action: { actionId: 17 },
        roleId: 2
      }
    };

    await api.post("/gpf-withdrawl/save", payload);

    alert("✅ GPF Withdrawal Application submitted successfully");

  } catch (err) {
    console.error(err);
    alert(err.response?.data || "Error while saving");
  }
};

  /* ================= JSX ================= */
  return (
    <div className="container">
      <h2>GPF Withdrawal Application</h2>

      {/* ================= EMPLOYEE DETAILS ================= */}
      <h3>Employee Details</h3>
      <div className="form-section">

        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Employee Code</label>
            {/*<input className="form-input" value={masterApiData?.empcode || ""} readOnly />*/}
     <input
  className="form-input"
  value={empCodeInput}
  onChange={(e) => setEmpCodeInput(e.target.value)}
/>
          </div>

          
        </div>


          <div className="info-grid">

<div className="info-card">
<div className="info-label">Employee Code</div>
<div className="info-value">{masterApiData?.empcode || "-"}</div>
</div>

<div className="info-card">
<div className="info-label">Employee Name</div>
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

<div className="info-card">
<div className="info-label">Date of Joining</div>
<div className="info-value">{formatDate(masterApiData?.dateofjoining)}</div>
</div>

<div className="info-card">
<div className="info-label">Retirement Date</div>
<div className="info-value">{formatDate(masterApiData?.dateofsuperannuation)}</div>
</div>

</div>

        </div>
      

      <hr />

      {/* ================= WITHDRAWAL DETAILS ================= */}
      {/* ================= WITHDRAWAL DETAILS ================= */}
<h3>Withdrawal Details</h3>
<div className="form-section">

  {/* ---- SYSTEM / API DATA (READ ONLY) ---- */}
  <div className="form-row">
    <div className="form-group">
      <label className="form-label">GPF Account No</label>
      <input
  className="form-input"
  value={gpfAccountInput}
  onChange={(e) => setGpfAccountInput(e.target.value)}
/>
    </div>
  
    {/* ---- USER INPUT ---- */}
  
    <div className="form-group">
     <label className="form-label">
  Amount of Withdrawal Requested <span className="required">*</span>
</label>
      <input
        className="form-input"
        name="amountofwithdrawlrequested"
        value={userInput.amountofwithdrawlrequested}
        onChange={handleUserChange}
      />
    </div>

    <div className="form-group">
      <label className="form-label">Date of Application <span className="required">*</span></label>
      <input
        className="form-input"
        type="date"
        name="dateofapplication"
        value={(userInput.dateofapplication)}
        onChange={handleUserChange}
      />
    </div>
    <div className="form-group">
            <label className="form-label">Concerned Officer</label>
            <input
              className="form-input"
              name="concernedofficername"
              value={userInput.concernedofficername}
              onChange={handleUserChange}
            />
          </div>
    <div className="form-group">
      <label className="form-label">Purpose of Withdrawal <span className="required">*</span></label>
    <textarea
  className={`form-input ${
    userInput.purposeofwithdrawl ? "filled" : ""
  }`}
  name="purposeofwithdrawl"
  value={userInput.purposeofwithdrawl}
  onChange={handleUserChange}
  placeholder="Enter purpose of withdrawal"
/>    </div>

  </div>

  <div className="form-row">
       
    <div className="form-group">
  <label className="form-label">Withdrawal Rule <span className="required">*</span></label>
   <select
  className="form-input"
    name="withdrawlrule"
  value={selectedRuleId}
  onChange={(e) => setSelectedRuleId(e.target.value)}
>
  <option value="">-- Select Withdrawal Reason --</option>

  {rules.map(rule => (
    <option key={rule.ruleId} value={rule.ruleId}>
      {rule.withdrawlReason}
    </option>
  ))}
</select>

</div>
  
  <div className="prior-withdrawal-box">

  <label className="prior-checkbox">
    <input
      type="checkbox"
      name="ispriorwithdrawlforsamepurpose"
      checked={userInput.ispriorwithdrawlforsamepurpose}
      onChange={handleUserChange}
    />
    Prior withdrawal for same purpose
  </label>

  <div className="prior-field">
    <label>Prior Amount</label>
    <input
      className="form-input"
      name="priorwithdrawlamount"
      disabled={!userInput.ispriorwithdrawlforsamepurpose}
      value={userInput.priorwithdrawlamount}
      onChange={handleUserChange}
    />
  </div>

  <div className="prior-field">
    <label>Financial Year</label>
    <input
      className="form-input"
      name="priorwithdrawlfinyear"
      disabled={!userInput.ispriorwithdrawlforsamepurpose}
      value={userInput.priorwithdrawlfinyear}
      onChange={handleUserChange}
    />
  </div>

</div>
  </div>
<div className="info-grid">
    <div className="info-card">
<div className="info-label">Basic Pay</div>
<div className="info-value">₹{detailsApiData?.basicpay || "-"}</div>
</div>

<div className="info-card">
<div className="info-label">Outstanding Balance As On</div>
<div className="info-value">{formatDate(getLastFinancialYearEndDate())}</div>
</div>

<div className="info-card">
<div className="info-label">Outstanding Balance</div>
<div className="info-value">₹{detailsApiData?.outstandingbalance || "-"}</div>
</div>

<div className="info-card">
<div className="info-label">Credit From</div>
<div className="info-value">{formatDate(getCurrentFinancialYearStartDate())}</div>
</div>

<div className="info-card">
<div className="info-label">Credit To</div>
<div className="info-value">{formatDate(getTodayDate())}</div>
</div>

<div className="info-card">
<div className="info-label">Total Credit Amount</div>
<div className="info-value">₹{detailsApiData?.totalcreditamount || "-"}</div>
</div>

<div className="info-card">
<div className="info-label">Refund After Outstanding</div>
<div className="info-value">
₹{detailsApiData?.refundafterdateofoutstandingbalance || "-"}
</div>
</div>

<div className="info-card">
<div className="info-label">Total Withdrawal Amount</div>
<div className="info-value">₹{detailsApiData?.totalwithdrawlamount || "-"}</div>
</div>

<div className="info-card">
<div className="info-label">Net Balance</div>
<div className="info-value">₹{detailsApiData?.netbalance || "-"}</div>
</div>

</div>

  
  {selectedRule && (
  <div className="rule-description-box">
    <strong>Rule Description:</strong>
    <div>{selectedRule.ruleDescription}</div>
  </div>
)}
</div>

{Object.keys(errors).length > 0 && (
  <div className="error-summary">
    <strong>Please fix the following errors:</strong>
    <ul>
      {Object.values(errors).map((msg, index) => (
        <li key={index}>{msg}</li>
      ))}
    </ul>
  </div>
)}
      <div className="form-row-center">
        {showVerification && (
  <div className="verification-box">

    <h3>Verify Withdrawal</h3>

    <p>
      <strong>Net Balance:</strong> ₹{detailsApiData?.netbalance}
    </p>

    <p>
      <strong>Requested Withdrawal:</strong> ₹{userInput.amountofwithdrawlrequested}
    </p>

    <div className="verification-actions">
      <button onClick={confirmSubmit}>Confirm</button>
      <button onClick={() => setShowVerification(false)}>Cancel</button>
    </div>

  </div>
)}
        <button className="process-btn" onClick={handleSubmit}>
          Submit Application
        </button>
      </div>
    </div>
  );
};

export default GpfWithdrawlForm;
