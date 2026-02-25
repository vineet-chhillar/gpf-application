import { EMPLOYEES, GPF_DETAILS } from "./gpfDummyData";

export const getMasterByEmpCode = async (empcode) => {

  await new Promise(r => setTimeout(r, 300));

  return EMPLOYEES.find(e => e.empcode === empcode);
};

export const getDetailsByAccount = async (accountNo) => {

  await new Promise(r => setTimeout(r, 300));

  return GPF_DETAILS.find(d => d.gpfaccountno === accountNo);
};