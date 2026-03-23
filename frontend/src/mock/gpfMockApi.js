import { EMPLOYEES, GPF_DETAILS } from "./gpfDummyData";

/* MASTER API */

export const getMasterByEmpCode = async (empcode) => {

  await new Promise(r => setTimeout(r, 300));

  return EMPLOYEES.find(e => e.empcode === empcode);
};

/* DETAILS BY ACCOUNT (used by Advance page) */

export const getDetailsByAccount = async (accountNo) => {

  await new Promise(r => setTimeout(r, 300));

  return GPF_DETAILS.find(d => d.gpfaccountno === accountNo);
};

/* DETAILS BY PAN (used by Withdrawal page) */

export const getDetailsByPan = async (panNo) => {

  await new Promise(r => setTimeout(r, 300));

  return GPF_DETAILS.find(d => d.panno === panNo);
};