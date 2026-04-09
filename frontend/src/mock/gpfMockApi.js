import { EMPLOYEES, GPF_DETAILS } from "./gpfDummyData";
import api from "../api/axios";
/* MASTER API */

{/*export const getMasterByEmpCode = async (empcode) => {

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
export const getDetailsByPan = async (panNo) => {
  try {
    const res = await api.get(`/gpf-withdrawl/details/${panNo}`);

    console.log("RAW DETAILS:", res.data);

    return mapDetailsApi(res.data); 

  } catch (error) {
    console.error("Details API Error:", error);
    return null;
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

    // 🔥 FIXED FIELD NAMES
    concernedofficername:
      data.nameoftheofficermaintainingthePFAccount || "",

    ispriorwithdrawlforsamepurpose:
      data.ispriorwithdrawalforsamepurpose === "yes",

    priorwithdrawlamount:
      data.priorwithdrawalamount || "",

    priorwithdrawlfinyear: formatFinancialYear(
      data.priorwithdrawalfinyear
    )
  };
};
const formatFinancialYear = (fy) => {
  if (!fy) return "";

  const parts = fy.split("-");
  if (parts.length !== 2) return fy;

  return `${parts[0]}-${parts[1].padStart(2, "0")}`;
};