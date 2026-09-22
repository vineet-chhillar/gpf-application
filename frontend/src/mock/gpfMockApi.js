import { EMPLOYEES, GPF_DETAILS } from "./gpfDummyData";
import api from "../api/axios";
/* MASTER API */

{/*export const getMasterByEmpCode = async (empcode) => {

  console.log("Fetching master for empcode:", empcode);
  await new Promise(r => setTimeout(r, 300));

  return EMPLOYEES.find(e => e.empcode === empcode);
};*/}

export const getMasterByEmpCode = async (empcode) => {
  try {

    const res = await api.get(`/gpf-withdrawl/master/${empcode}`);

    console.log("RES.DATA:", res.data);

    return res.data;   // ✅ DIRECT RETURN

  } catch (error) {
    console.error("Master API Error:", error);
    return null;
  }
};


const mapMasterApi = (data) => {
  if (!data) return null;

  const roles = data.functional_roles || [];

  return {
    empcode: data.emp_code,   // ✅ now works
    empname: data.empname,
    designation: data.designation,

    empdivision: roles.map(r => r.div_name).join(", "),
    functionalpost: roles.map(r => r.post_name).join(", "),

    empmobileno: data.empmobileno,
    empemailid: data.empemailid,
    dateofjoining: data.dateofjoining,
    dateofsuperannuation: data.dateofsuperannuation,
    panno: data.panno,
    roles: roles
  };
};


/* DETAILS BY PAN (used by Withdrawal page) */

{/*export const getDetailsByPan = async (panNo) => {

  await new Promise(r => setTimeout(r, 300));

  return GPF_DETAILS.find(d => d.panno === panNo);
};*/}

{/*export const getDetailsByPan = async (panNo) => {

  await new Promise(r => setTimeout(r, 300));

  const HARD_CODED_PAN = "ABMPA1395B";

  return GPF_DETAILS.find(d => d.panno === HARD_CODED_PAN);
};*/}


export const getDetailsByPan = async (pan) => {
  try {
    const response = await api.get(`/gpf-withdrawl/details/${pan}`);

    console.log("FULL AXIOS RESPONSE:", response);

    const res = response.data;

    console.log("ACTUAL DATA:", res);

    // 🔴 Case 1: No data found
    if (
      res?.status === "OK" &&
      res?.message === "No data found"
    ) {
      return null; // 👈 important
    }

    // 🔴 Case 2: Unexpected shape
    if (!res || typeof res !== "object") {
      throw new Error("Invalid API response");
    }

    // 🔴 Case 3: Success → map fields
    return {
      basicpay: res.basicpay,
      closingbalance: res.closingbalance,
      outstandingbalance: res.outstandingbalance,
      totalcreditamount: res.totalcreditamount,
      refundafterdateofoutstandingbalance:
        res.refundafterdateofoutstandingbalance,
      totalwithdrawlamount: res.totalwithdrawlamount,
      gpfaccountno: res.gpfaccountno,
      concernedofficername:
        res.nameoftheofficermaintainingthePFAccount,
      netbalance: res.netbalance
    };

  } catch (error) {
    console.error("DETAIL API ERROR:", error);
    console.error("ERROR RESPONSE:", error.response);

    // 🔴 Extract backend message if available
    const backendMessage =
      error?.response?.data?.message ||
      error?.message ||
      "Failed to fetch GPF details";

    throw new Error(backendMessage);
  }
};
const mapDetailsApi = (data) => {
  if (!data) return null;

  return {
    panno: data.panno,
    gpfaccountno: data.gpfaccountno,

    basicpay: data.basicpay,

    outstandingbalance: data.outstandingbalance,

    closingbalance: data.closingbalance,

    totalcreditamount: data.totalcreditamount,

    refundafterdateofoutstandingbalance:
      data.refundafterdateofoutstandingbalance,

    totalwithdrawlamount: data.totalwithdrawlamount,

    netbalance: data.netbalance,

    concernedofficername:
      data.nameoftheofficermaintainingthePFAccount || "",

    ispriorwithdrawlforsamepurpose:
      data.ispriorwithdrawalforsamepurpose === "yes",

    priorwithdrawlamount:
      data.priorwithdrawalamount || "",

    priorwithdrawlfinyear:
      formatFinancialYear(data.priorwithdrawalfinyear)
  };
};
const formatFinancialYear = (fy) => {
  if (!fy) return "";

  const parts = fy.split("-");
  if (parts.length !== 2) return fy;

  return `${parts[0]}-${parts[1].padStart(2, "0")}`;
};