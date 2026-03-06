import React, { useState } from "react";
import WithdrawlRuleMaster from "./WithdrawlRuleMaster";
import AdvanceRuleMaster from "./AdvanceRuleMaster";
import "../styles/WithdrawlRuleMaster.css";

const GpfRuleMaster = () => {

const [ruleType,setRuleType] = useState("withdrawl");

return (

<div className="rule-master-container">

{/*<h2>GPF Rule Master</h2>*/}

<div className="type-selector">

<label className={ruleType==="withdrawl" ? "active" : ""}>
<input
type="radio"
value="withdrawl"
checked={ruleType==="withdrawl"}
onChange={()=>setRuleType("withdrawl")}
/>
<span>Withdrawal Rules</span>
</label>

<label className={ruleType==="advance" ? "active" : ""}>
<input
type="radio"
value="advance"
checked={ruleType==="advance"}
onChange={()=>setRuleType("advance")}
/>
<span>Advance Rules</span>
</label>

</div>

<div className="rule-master-body">

{ruleType==="withdrawl" && <WithdrawlRuleMaster/>}

{ruleType==="advance" && <AdvanceRuleMaster/>}

</div>

</div>

);

};

export default GpfRuleMaster;