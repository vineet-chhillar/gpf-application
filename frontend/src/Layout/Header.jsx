import React from "react";
import "./Header.css";

const Header = ({ title }) => {

  return (
    <div className="header">
      <div className="header-title">
  💼 GPF/CPF Processing System
</div>


     <div className="header-right">
  <div className="user-info">
    <span className="user-icon">👤</span>
    <span className="user-name">xyz</span>
  </div>

  <button className="env-badge-btn">
    <svg
    width="16"
    height="16"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    className="back-icon"
  >
    <polyline points="15 18 9 12 15 6" />
  </svg> Back To Digital
  </button>
</div>


    </div>
  );
};

export default Header;
