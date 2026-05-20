import React, { useState, useEffect } from "react";
import api from "../api/axios";
import "../styles/GpfForm.css";
import { useRef } from "react";
import {
  getMasterByEmpCode,
  getDetailsByPan
} from "../mock/gpfMockApi";

//api.get("/gpf/withdrawal-rules/active")


const GpfWithdrawlForm = () => {



const [empCodeInput, setEmpCodeInput] = useState("");
const [gpfAccountInput, setGpfAccountInput] = useState("");
const [dropdownOpen, setDropdownOpen] = useState(false);
const [hoveredRule, setHoveredRule] = useState(null);
  /* ================= API DATA ================= */
  const [masterApiData, setMasterApiData] = useState(null);
  const [detailsApiData, setDetailsApiData] = useState(null);

  const [showVerification, setShowVerification] = useState(false);
{/*const [showSuccess, setShowSuccess] = useState(false);*/}
const [messageModal, setMessageModal] = useState({
  open: false,
  type: "success", // success | error | warning
  title: "",
  message: ""
});
const openMessageModal = ({
  type = "success",
  title,
  message
}) => {

  setMessageModal({
    open: true,
    type,
    title,
    message
  });
};
  const formatDate = (dateString) => {
  if (!dateString) return "";

  const date = new Date(dateString);

  const day = String(date.getDate()).padStart(2, "0");
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const year = date.getFullYear();

  return `${day}/${month}/${year}`;
};
const dropdownRef = useRef(null);
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
    amountofwithdrawlrequested: "",
    purposeofwithdrawl: "",
     concernedofficername: "", 
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
const resetForm = () => {

  setMasterApiData(null);
  setDetailsApiData(null);

  setEmpCodeInput("");
  setGpfAccountInput("");

  setSelectedRuleId("");

  setUserInput({
    amountofwithdrawlrequested: "",
    purposeofwithdrawl: "",
    concernedofficername: "",
    ispriorwithdrawlforsamepurpose: false,
    priorwithdrawlamount: "",
    priorwithdrawlfinyear: "",
    dateofapplication: getTodayDate()
  });

  setErrors({});
  setShowVerification(false);
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

    console.log("MAPPED MASTER FROM API:", master);

    if (master) {
      setMasterApiData(master);
    } else {
      setMasterApiData(null);
      setDetailsApiData(null);
      setGpfAccountInput("");
       openMessageModal({
    type: "warning",
    title: "Employee Not Found",
    message:
      "No employee exists for the entered employee code."
  });
    }

  } catch (err) {

    console.error("Master fetch failed", err);

    setMasterApiData(null);
    setDetailsApiData(null);
    setGpfAccountInput("");
  }
};

useEffect(() => {
  if (!detailsApiData) return;

  setUserInput(prev => ({
    ...prev,
    concernedofficername:
      detailsApiData.concernedofficername || prev.concernedofficername,

    
  }));

}, [detailsApiData]);
useEffect(() => {

  if (!masterApiData?.panno) {
    setDetailsApiData(null); // 🔥 clear if no PAN
    return;
  }

  const timer = setTimeout(async () => {

    try {

      const pan = masterApiData.panno.trim().toUpperCase();

      const details = await getDetailsByPan(pan);
console.log("DETAILS FROM FRONTEND API:", details);
      if (details) {
  setDetailsApiData(details);
  setGpfAccountInput(details.gpfaccountno || ""); // ✅ NEW
} else {
  setDetailsApiData(null);
  setGpfAccountInput(""); // ✅ clear
}

    } catch (err) {

      console.error("Details fetch failed", err);
      setDetailsApiData(null); // ❗ API failed

    }

  }, 400);

  return () => clearTimeout(timer);

}, [masterApiData]);

{/*useEffect(() => {

  if (!empCodeInput) return;

  setGpfAccountInput(`GPF-NIC-${empCodeInput}`);

}, [empCodeInput]);*/}


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
      
      openMessageModal({
  type: "warning",
  title: "Enter Employee Code",
  message: "Enter Employee Code to fetch details."
});
      return;
    }

    if (!gpfAccountInput) {
      
      openMessageModal({
  type: "warning",
  title: "Enter GPF Account No",
  message: "Enter GPF Account No to fetch details."
});
      return;
    }

    const master = await getMasterByEmpCode(empCodeInput);

    const details = await getDetailsByPan(master.panno);

    if (!master) {
      
      openMessageModal({
  type: "warning",
  title: "Employee Not Found",
  message: "No employee exists for the entered employee code."
});
      return;
    }

    if (!details) {
      
      openMessageModal({
  type: "warning",
  title: "GPF account not found",
  message: "GPF account not found for the employee's PAN."
});
      return;
    }

    setMasterApiData(master);
    setDetailsApiData(details);

  } catch (err) {
    console.error(err);
    
    openMessageModal({
  type: "warning",
  title: "Failed to load employee data",
  message: "Failed to load employee data. Please try again."
});
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
{/*end for withdrawal rule calculation*/}

const calculateWithdrawalEligibility = ({
  basicPay,
  balanceAmount,
  requestedAmount,
  dateOfJoining,
  retirementDate,
  selectedRule
}) => {

  if (!selectedRule) {
    return {
      isValid: false,
      eligibleAmount: 0,
      message: "Withdrawal rule not selected"
    };
  }

  // 1. Rule active
  if (!selectedRule.isActive) {
    return {
      isValid: false,
      eligibleAmount: 0,
      message: "Selected withdrawal rule is inactive"
    };
  }

  const today = new Date();

  // 2. Service years
  const joiningDate = new Date(dateOfJoining);

  let serviceYears =
    today.getFullYear() - joiningDate.getFullYear();

  const joiningMonthDiff =
    today.getMonth() - joiningDate.getMonth();

  if (
    joiningMonthDiff < 0 ||
    (
      joiningMonthDiff === 0 &&
      today.getDate() < joiningDate.getDate()
    )
  ) {
    serviceYears--;
  }

  if (
    selectedRule.minServiceYears &&
    serviceYears < selectedRule.minServiceYears
  ) {
    return {
      isValid: false,
      eligibleAmount: 0,
      message:
        `Minimum ${selectedRule.minServiceYears} years service required`
    };
  }

  // 3. Retirement window rule
  if (selectedRule.retirementWindowYrs) {

    const retirement = new Date(retirementDate);

    let yearsLeft =
      retirement.getFullYear() - today.getFullYear();

    const retirementMonthDiff =
      retirement.getMonth() - today.getMonth();

    if (
      retirementMonthDiff < 0 ||
      (
        retirementMonthDiff === 0 &&
        retirement.getDate() < today.getDate()
      )
    ) {
      yearsLeft--;
    }

    if (yearsLeft <= selectedRule.retirementWindowYrs) {

      const eligibleAmount =
        (Number(balanceAmount) * 90) / 100;

      return {
        isValid:
          Number(requestedAmount) <= eligibleAmount,
        eligibleAmount,
        message:
          Number(requestedAmount) <= eligibleAmount
            ? ""
            : `Maximum eligible amount is ₹${eligibleAmount.toFixed(2)}`
      };
    }
  }

  // 4. Percentage calculation
  let percentageAmount = Infinity;

  if (selectedRule.maxPercentage) {
    percentageAmount =
      (Number(balanceAmount) *
        Number(selectedRule.maxPercentage)) / 100;
  }

  // 5. Months pay calculation
  let monthsPayAmount = Infinity;

  if (selectedRule.maxMonthsPay) {
    monthsPayAmount =
      Number(basicPay) *
      Number(selectedRule.maxMonthsPay);
  }

  // 6. Final eligible amount
  const eligibleAmount = Math.min(
    percentageAmount,
    monthsPayAmount
  );

  return {
    isValid:
      Number(requestedAmount) <= eligibleAmount,
    eligibleAmount,
    message:
      Number(requestedAmount) <= eligibleAmount
        ? ""
        : `Maximum eligible amount is ₹${eligibleAmount.toFixed(2)}`
  };
};

{/*end for withdrawal rule calculation*/}
const eligibilityResult =
  selectedRule &&
  detailsApiData &&
  masterApiData
    ? calculateWithdrawalEligibility({
        basicPay: detailsApiData.basicpay,
        balanceAmount: detailsApiData.closingbalance,
        requestedAmount:
          userInput.amountofwithdrawlrequested || 0,

        dateOfJoining:
          masterApiData.dateofjoining,

        retirementDate:
          masterApiData.dateofsuperannuation,

        selectedRule
      })
    : null;
  {/*const buildGpfAccountNo = (empcode) => `GPF-NIC-${empcode}`;*/}
const ONLY_CHARS_REGEX = /^[A-Za-z ]+$/;
const validateForm = () => {
  const newErrors = {};

  // ---- MASTER ----
  if (!userInput.concernedofficername.trim()) {
  newErrors.concernedofficername = "Concerned Officer is required";
} else if (userInput.concernedofficername.length > 100) {
  newErrors.concernedofficername =
    "Cannot exceed 100 characters";
} {/*else if (!ONLY_CHARS_REGEX.test(userInput.concernedofficername)) {
  newErrors.concernedofficername =
    "Only alphabets allowed";
}*/}


  // ---- DETAILS (USER INPUT) ----
  const amount = Number(userInput.amountofwithdrawlrequested);

  if (!amount || amount <= 0) {

  newErrors.amountofwithdrawlrequested =
    "Withdrawal amount must be greater than 0";

} else {

  const validation =
    calculateWithdrawalEligibility({

      basicPay: detailsApiData.basicpay,
      balanceAmount: detailsApiData.closingbalance,
      requestedAmount: amount,

      dateOfJoining:
        masterApiData.dateofjoining,

      retirementDate:
        masterApiData.dateofsuperannuation,

      selectedRule
    });

  if (!validation.isValid) {

    newErrors.amountofwithdrawlrequested =
      validation.message;
  }
}
  {/*if (!amount || amount <= 0) {
    newErrors.amountofwithdrawlrequested =
      "Withdrawal amount must be greater than 0";
  } else if (amount > Number(detailsApiData.netbalance)) {
    newErrors.amountofwithdrawlrequested =
      "Withdrawal amount cannot exceed net balance";
  }*/}

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
if (userInput.ispriorwithdrawlforsamepurpose) {

  if (
    !userInput.priorwithdrawlamount ||
    Number(userInput.priorwithdrawlamount) <= 0
  ) {

    newErrors.priorwithdrawlamount =
      "Prior withdrawal amount is required";
  }

  if (!userInput.priorwithdrawlfinyear) {

    newErrors.priorwithdrawlfinyear =
      "Financial year is required";
  }
}
  setErrors(newErrors);
  return Object.keys(newErrors).length === 0;
};
useEffect(() => {
  const handleClickOutside = (event) => {
    if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
      setDropdownOpen(false);
      setHoveredRule(null);   // 🔥 IMPORTANT
    }
  };

  document.addEventListener("mousedown", handleClickOutside);

  return () => {
    document.removeEventListener("mousedown", handleClickOutside);
  };
}, []);
const confirmSubmit = async () => {

  try {

    const payload = {
      master: {
         ...masterApiData,
  empcode: masterApiData.empcode || masterApiData.empCode  
      },
      details: {
        ...detailsApiData,

       gpfaccountno: detailsApiData.gpfaccountno,
panno: masterApiData.panno,
        creditfromdate: getCurrentFinancialYearStartDate(),
        credittodate: getTodayDate(),
        dateofoutstandingbalance: getCurrentFinancialYearStartDate(),

        withdrawlfromdate: getCurrentFinancialYearStartDate(),
        withdrawltodate: getTodayDate(),

        amountofwithdrawlrequested: userInput.amountofwithdrawlrequested,
        purposeofwithdrawl: userInput.purposeofwithdrawl,
        withdrawlrule: Number(selectedRuleId),

       concernedofficername:
  detailsApiData?.concernedofficername || userInput.concernedofficername,

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

    console.log("FINAL PAYLOAD:", payload);
    await api.post("/gpf-withdrawl/save", payload);

    setShowVerification(false);

    openMessageModal({
  type: "success",
  title: "Withdrawal Submitted Successfully",
  message:
    "Your GPF withdrawal application has been submitted."
});
resetForm();

  } catch (err) {

    console.error(err);
    
openMessageModal({
  type: "warning",
  title: err.response?.data || "Error while saving",
  message: err.response?.data || "Error while saving"
});
  }

};

const handleSubmit = () => {

  if (!validateForm()) return;

  if (!masterApiData || !masterApiData.empcode) {
    
    openMessageModal({
  type: "warning",
  title: "Employee data not loaded properly",
  message: "Employee data not loaded properly. Please check employee code and try again."
});
    return;
  }

  if (!detailsApiData) {
    
    openMessageModal({
  type: "warning",
  title: "GPF account not found",
  message: "GPF account not found for the employee's PAN."
});
    return;
  }

  setShowVerification(true);
};

  /* ================= JSX ================= */
  return (
    <div className="container">
      {/*<h2>GPF Withdrawal Application</h2>*/}

      {/* ================= EMPLOYEE DETAILS ================= */}
      <div className="section-header">

  <div className="section-header-icon">

    <svg
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M20 21a8 8 0 0 0-16 0"/>
      <circle cx="12" cy="8" r="4"/>
    </svg>

  </div>

  <div className="section-header-text">

    <h3>
      Employee Details
    </h3>

    <span>
      Employee and service information
    </span>

  </div>

</div>
      <div className="form-section">

        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Employee Code <span className="required">*</span></label>
            {/*<input className="form-input" value={masterApiData?.empcode || ""} readOnly />*/}
 <input
  className="form-input"
  value={empCodeInput}
  onChange={(e) => {
    setEmpCodeInput(e.target.value);

    // 🔥 clear old data instantly
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
<div className="info-value">{userInput.dateofapplication}</div>
</div>

{/*<div className="form-group">
      <label className="form-label">Date of Application <span className="required">*</span></label>
      <input
  className="form-input"
  type="date"
  name="dateofapplication"
  value={userInput.dateofapplication}
  onChange={handleUserChange}
  readOnly
/>
    </div>*/}


{/*<div className="form-group">
      <label className="form-label">GPF Account No</label>
      <input
  className="form-input"
  value={gpfAccountInput}
  readOnly
/>
    </div>*/}


</div>

        </div>
      

      <hr />

      {/* ================= WITHDRAWAL DETAILS ================= */}
      {/* ================= WITHDRAWAL DETAILS ================= */}
<div className="section-header">

  <div className="section-header-icon">

    <svg
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
      <polyline points="14 2 14 8 20 8"/>
      <line x1="8" y1="13" x2="16" y2="13"/>
      <line x1="8" y1="17" x2="13" y2="17"/>
    </svg>

  </div>

  <div className="section-header-text">

    <h3>
      Withdrawal Request
    </h3>

    <span>
      GPF Withdrawal Application Form
    </span>

  </div>

</div>
<div className="form-section">

  {/* ---- SYSTEM / API DATA (READ ONLY) ---- */}
  <div className="form-row-compact">
    
  
    {/* ---- USER INPUT ---- */}
  <div className="form-group">
  <label className="form-label">Withdrawal Rule <span className="required">*</span></label>
  <div className="custom-dropdown" ref={dropdownRef}>

  <div
    className="dropdown-selected"
    onClick={() => setDropdownOpen(!dropdownOpen)}
  >
    {selectedRule
      ? selectedRule.withdrawlReason
      : "-- Select Withdrawal Reason --"}
  </div>

  {dropdownOpen && (
    <div className="dropdown-menu">
      {rules.map(rule => (
        <div
          key={rule.ruleId}
          className="dropdown-item"
          onClick={() => {
            setSelectedRuleId(rule.ruleId);
            setDropdownOpen(false);
            setHoveredRule(null);  
          }}
          onMouseEnter={() => setHoveredRule(rule)}
          onMouseLeave={() => setHoveredRule(null)}
        >
          {rule.withdrawlReason}
        </div>
      ))}
    </div>
  )}

  {/* 🔥 MODAL HERE (inside dropdown) */}
  {dropdownOpen && hoveredRule && (
    <div className="rule-hover-modal">
      <strong>{hoveredRule.withdrawlReason}</strong>
      <p>{hoveredRule.ruleDescription}</p>
    </div>
  )}

</div>

</div>
</div>
{eligibilityResult && (
  <div className="eligibility-box">

    <span className="eligibility-label">
      Eligible Amount:
    </span>

    <span className="eligibility-value">
      ₹{Number(
        eligibilityResult.eligibleAmount || 0
      ).toLocaleString("en-IN", {
        maximumFractionDigits: 2
      })}
    </span>

    {!eligibilityResult.isValid &&
      userInput.amountofwithdrawlrequested && (
        <span className="eligibility-warning">
          {eligibilityResult.message}
        </span>
      )}

  </div>
)}
<div className="form-row-compact">

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

<div className="form-group">
  <label className="form-label">
    Name of accounts officer maintaining the PF account <span className="required">*</span>
  </label>

 <input
  className="form-input"
  name="concernedofficername"
  value={userInput.concernedofficername || ""}
  onChange={handleUserChange}
  placeholder="Enter officer name"
  readOnly={!!detailsApiData?.concernedofficername}
/>
</div>
  
    

  </div>

  <div className="form-row">
       
    
  
  <div className="prior-withdrawal-box">

  <label className="prior-checkbox">
    <input
  type="checkbox"
  name="ispriorwithdrawlforsamepurpose"
  checked={userInput.ispriorwithdrawlforsamepurpose}
  onChange={handleUserChange}
  
/>
    Whether any withdrawl was taken for the same purpose earlier.
    If so, indicate the amount and the year.
  </label>

  <div className="prior-field">
   <label>
  Prior Amount
  {userInput.ispriorwithdrawlforsamepurpose && (
    <span className="required">*</span>
  )}
</label>
    <input
  className="form-input"
  name="priorwithdrawlamount"
  disabled={!userInput.ispriorwithdrawlforsamepurpose}
  value={userInput.priorwithdrawlamount}
  onChange={handleUserChange}
/>
  </div>

  <div className="prior-field">

  <label>
  Financial Year
  {userInput.ispriorwithdrawlforsamepurpose && (
    <span className="required">*</span>
  )}
</label>

  <select
    className="form-input"
    name="priorwithdrawlfinyear"
    disabled={!userInput.ispriorwithdrawlforsamepurpose}
    value={userInput.priorwithdrawlfinyear}
    onChange={handleUserChange}
  >

    <option value="">
      Select Financial Year
    </option>

    {Array.from(
      { length: 2025 - 1980 + 1 },
      (_, index) => {

        const startYear = 2025 - index;
        const endYear =
          String(startYear + 1).slice(-2);

        const fy =
          `${startYear}-${endYear}`;

        return (
          <option key={fy} value={fy}>
            {fy}
          </option>
        );
      }
    )}

  </select>

</div>

</div>
  </div>
<div className="info-grid">

  

    <div className="info-card">
<div className="info-label">Basic Pay</div>
<div className="info-value">₹{detailsApiData?.basicpay || "-"}</div>
</div>

<div className="info-card">
<div className="info-label">Closing Balance As On</div>
<div className="info-value">{formatDate(getLastFinancialYearEndDate())}</div>
</div>

<div className="info-card">
<div className="info-label">Closing Balance</div>
<div className="info-value">₹{detailsApiData?.closingbalance || "-"}</div>
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
<div className="info-label">Withdrawal From</div>
<div className="info-value">{formatDate(getCurrentFinancialYearStartDate())}</div>
</div>

<div className="info-card">
<div className="info-label">Withdrawal To</div>
<div className="info-value">{formatDate(getTodayDate())}</div>
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

  
{/*}  {selectedRule && (
  <div className="rule-description-box">
    <strong>Rule Description:</strong>
    <div>{selectedRule.ruleDescription}</div>
  </div>
)}*/}


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
  <div className="modal-overlay">

    <div className="modal-box">

      <h3>Verify Withdrawal</h3>

      <div className="modal-content">

        <div className="modal-row">
          <span>Net Balance</span>
          <strong>₹{detailsApiData?.netbalance}</strong>
        </div>

        <div className="modal-row">
          <span>Withdrawal Requested</span>
          <strong>₹{userInput.amountofwithdrawlrequested}</strong>
        </div>

        <div className="modal-row highlight">
          <span>Balance After Withdrawal</span>
          <strong>
            ₹{Number(detailsApiData?.netbalance || 0) - Number(userInput.amountofwithdrawlrequested || 0)}
          </strong>
        </div>

      </div>

      <div className="modal-actions">
        <button className="confirm-btn" onClick={confirmSubmit}>
          Confirm
        </button>

        <button
          className="cancel-btn"
          onClick={() => setShowVerification(false)}
        >
          Cancel
        </button>
      </div>

    </div>

  </div>
)}

{/* 🔥 ADD HERE */}
{/*{hoveredRule && (
  <div className="rule-hover-modal">
    <strong>{hoveredRule.withdrawlReason}</strong>
    <p>{hoveredRule.ruleDescription}</p>
  </div>
)}*/}

        <button className="process-btn" onClick={handleSubmit}>
          Submit Application
        </button>
        {/*{showSuccess && (

<div className="modal-overlay">

<div className="modal-box success">

<h3>✅ Withdrawal Submitted Successfully</h3>

<p>Your GPF withdrawal application has been submitted.</p>

<button
className="confirm-btn"
onClick={() => setShowSuccess(false)}
>
OK
</button>

</div>

</div>

)}*/}
      </div>
      {messageModal.open && (

<div className="modal-overlay">

  <div className={`modal-box ${messageModal.type}`}>

    <h3>

      {messageModal.type === "success" && "✅ "}
      {messageModal.type === "error" && "❌ "}
      {messageModal.type === "warning" && "⚠️ "}

      {messageModal.title}

    </h3>

    <p>{messageModal.message}</p>

    <button
      className="confirm-btn"
      onClick={() =>
        setMessageModal(prev => ({
          ...prev,
          open: false
        }))
      }
    >
      OK
    </button>

  </div>

</div>

)}
    </div>
    
  );
  
};

export default GpfWithdrawlForm;
