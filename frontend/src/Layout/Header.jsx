import React from "react";
import "./Header.css";

const Header = ({ title }) => {

  const getInitials = (name) =>
    name.split(" ").map(n => n[0]).join("").toUpperCase();

  // 🔥 Add this mapping logic
  const getTitle = (type) => {
    if (!type) return "GPF Application";

    switch (type.toLowerCase()) {
      case "withdrawl": // or "withdrawal"
        return "GPF Withdrawal Application";
      case "advance":
        return "GPF Advance Application";
      default:
        return type;
    }
  };

  return (
    <div className="header">
  <div className="header-title">
    <span className="title-icon">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
        stroke="currentColor" strokeWidth="2">
        <rect x="3" y="7" width="18" height="13" rx="2"/>
        <path d="M16 3H8v4h8V3z"/>
      </svg>
    </span>

    <span>GPF/CPF Processing System  </span>

    {/* 🔥 Center ONLY this */}
    <span className="dynamic-title">
      {getTitle(title)}
    </span>
  </div>


      <div className="header-right">

        <div className="back-link">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" strokeWidth="2">
            <polyline points="15 18 9 12 15 6" />
          </svg>
          <span>Back to Digital</span>
        </div>

        <div className="divider"></div>

        <div className="user-box">
          <div className="user-avatar">
            {getInitials("Vineet Chhillar")}
          </div>
          <div className="user-text">
            <div className="user-label">Logged in as</div>
            <div className="user-name">Vineet</div>
          </div>
        </div>

      </div>
    </div>
  );
};

export default Header;