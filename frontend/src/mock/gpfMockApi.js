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

    console.log("RAW MASTER:", res.data);

    // backend already mapped → NO need to map again
    return res.data;

  } catch (error) {
    console.error("Master API Error:", error);
    return null;
  }
};
const mapMasterApi = (data) => {
  if (!data) return null;

  const roles = data.functional_roles || [];

  return {
    empcode: data.emp_code,
    empname: data.empname,
    designation: data.designation,

    
    empdivision: roles[0]?.div_name || "",
    functionalpost: roles[0]?.post_id || null,

    empmobileno: data.empmobileno,
    empemailid: data.empemailid,
    dateofjoining: data.dateofjoining,
    dateofsuperannuation: data.dateofsuperannuation,
    panno: data.panno,

    
    roles: roles
  };
};
/* DETAILS BY ACCOUNT (used by Advance page) */

{/*export const getDetailsByAccount = async (accountNo) => {

  await new Promise(r => setTimeout(r, 300));

  return GPF_DETAILS.find(d => d.gpfaccountno === accountNo);
};*/}

/* DETAILS BY PAN (used by Withdrawal page) */

export const getDetailsByPan = async (panNo) => {

  await new Promise(r => setTimeout(r, 300));

  return GPF_DETAILS.find(d => d.panno === panNo);
};