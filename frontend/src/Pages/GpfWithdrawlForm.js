import React, { useState, useEffect } from "react";
import api from "../api/axios";
import "../styles/GpfWithdrawlForm.css";
import { useRef } from "react";


api.get("/gpf/withdrawal-rules/active")


const GpfWithdrawlForm = () => {

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
const DEFAULT_MASTER_DATA = {
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
  netbalance: 650000};



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
      {/*const masterRes = await api.get("/gpf/master");
      const detailsRes = await api.get("/gpf/details");

      setMasterApiData(masterRes.data);
      setDetailsApiData(detailsRes.data);*/}
      // ✅ MOCK DEFAULT DATA
      setMasterApiData(DEFAULT_MASTER_DATA);
      setDetailsApiData(DEFAULT_DETAILS_DATA);
      // ================= WITHDRAWAL RULES =================
      // When API is ready, uncomment this:
       {/*const rulesRes = await api.get("/gpf/withdrawal-rules/active");
       
       setWithdrawalRules(rulesRes.data);*/}

      // TEMP MOCK (API STRUCTURE FINALIZED)
      
    } catch (err) {
      console.error(err);
      alert("Failed to load employee data");
    }
  };

  loadData();
}, []);

  /* ================= HANDLERS ================= */
  const handleUserChange = (e) => {
    const { name, value, type, checked } = e.target;

    setUserInput(prev => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value
    }));
  };

  const buildGpfAccountNo = (empcode) => `GPF-ACC-${empcode}`;
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
  value={masterApiData?.empcode || ""}
  onChange={(e) =>
    setMasterApiData(prev => ({
      ...prev,
      empcode: e.target.value
    }))
  }
/>
          </div>

          <div className="form-group">
            <label className="form-label">Employee Name</label>
           <input
  className="form-input"
  value={masterApiData?.empname || ""}
  onChange={(e) =>
    setMasterApiData(prev => ({
      ...prev,
      empname: e.target.value
    }))
  }
/>
          </div>

          <div className="form-group">
            <label className="form-label">Designation</label>
            <input className="form-input" value={masterApiData?.designation || ""} readOnly />
          </div>

          <div className="form-group">
            <label className="form-label">Division</label>
            <input className="form-input" value={masterApiData?.empdivision || ""} readOnly />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Mobile No</label>
            <input className="form-input" value={masterApiData?.empmobileno || ""} readOnly />
          </div>

          <div className="form-group">
            <label className="form-label">Email ID</label>
            <input className="form-input" value={masterApiData?.empemailid || ""} readOnly />
          </div>

          <div className="form-group">
            <label className="form-label">Date of Joining</label>
            <input className="form-input" value={formatDate(masterApiData?.dateofjoining)} readOnly />
          </div>

          <div className="form-group">
            <label className="form-label">Date of Superannuation</label>
            <input className="form-input" value={formatDate(masterApiData?.dateofsuperannuation)} readOnly />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label className="form-label">Concerned Officer</label>
            <input
              className="form-input"
              name="concernedofficername"
              value={userInput.concernedofficername}
              onChange={handleUserChange}
            />
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
        value={
          masterApiData
            ? buildGpfAccountNo(masterApiData.empcode)
            : ""
        }
        readOnly
      />
    </div>

    <div className="form-group">
      <label className="form-label">Basic Pay</label>
      <input
        className="form-input"
        value={detailsApiData?.basicpay || ""}
        readOnly
      />
    </div>

    <div className="form-group">
  <label className="form-label">Outstanding Balance As On</label>
  <input
  className="form-input"
  value={formatDate(getLastFinancialYearEndDate())}
  readOnly
/>
</div>


    <div className="form-group">
      <label className="form-label">Outstanding Balance</label>
      <input
        className="form-input"
        value={detailsApiData?.outstandingbalance || ""}
        readOnly
      />
    </div>
  </div>

  <div className="form-row">
    <div className="form-group">
  <label className="form-label">Credit From</label>
  <input
    className="form-input"
    value={formatDate(getCurrentFinancialYearStartDate())}
    readOnly
  />
</div>

<div className="form-group">
  <label className="form-label">Credit To</label>
  <input
    className="form-input"
    value={formatDate(getTodayDate())}
    readOnly
  />
</div>


    <div className="form-group">
      <label className="form-label">Total Credit Amount</label>
      <input
        className="form-input"
        value={detailsApiData?.totalcreditamount || ""}
        readOnly
      />
    </div>

    <div className="form-group">
      <label className="form-label">
        Refund After Outstanding Balance
      </label>
      <input
        className="form-input"
        value={detailsApiData?.refundafterdateofoutstandingbalance || ""}
        readOnly
      />
    </div>
  </div>

  <div className="form-row">
    <div className="form-group">
  <label className="form-label">Withdrawal From</label>
  <input
    className="form-input"
    value={formatDate(getCurrentFinancialYearStartDate())}
    readOnly
  />
</div>

<div className="form-group">
  <label className="form-label">Withdrawal To</label>
  <input
    className="form-input"
    value={formatDate(getTodayDate())}
    readOnly
  />
</div>


    <div className="form-group">
      <label className="form-label">Total Withdrawal Amount</label>
      <input
        className="form-input"
        value={detailsApiData?.totalwithdrawlamount || ""}
        readOnly
      />
    </div>

    <div className="form-group">
      <label className="form-label">Net Balance</label>
      <input
        className="form-input"
        value={detailsApiData?.netbalance || ""}
        readOnly
      />
    </div>
  </div>

  {/* ---- USER INPUT ---- */}
  <div className="form-row">
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
