import React from "react";
import "./Header.css";
import { useAuth } from "../context/AuthContext";
const Header = ({ title }) => {

const { employee, loading, error, logout } = useAuth();

console.log("Employee from AuthContext:", employee);
console.log("Employee loading:", loading);
console.log("Employee error:", error);

const handleLogout = () => {
  window.location.replace("/GPF/logout");
};
const getInitials = (name) => {
  if (!name || typeof name !== "string") {
    console.log("sdfghfhfdh",employee?.empName);
    return "NA";
  }

  return name
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word[0].toUpperCase())
    .join("");
};

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

    {/* LEFT */}
    <div className="header-left">

      <div className="title-icon">
        <svg
          width="18"
          height="18"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
        >
          <rect x="3" y="7" width="18" height="13" rx="2"/>
          <path d="M16 3H8v4h8V3z"/>
        </svg>
      </div>

      <div className="header-system">

        <div className="system-title">
          GPF/CPF Processing System
        </div>

        <div className="system-subtitle">
            Office Automation Division
        </div>

      </div>

    </div>

    {/* CENTER PAGE TITLE */}
    <div className="dynamic-title">
      {getTitle(title)}
    </div>

    {/* RIGHT */}
    <div className="header-right">

      <div className="back-link">

        <svg
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
        >
          <polyline points="15 18 9 12 15 6"/>
        </svg>

        <span>
          Back to Digital
        </span>

      </div>

      <div className="divider"></div>

      <div className="user-box">

        <div className="user-avatar">
          {getInitials(employee?.empName)}
        </div>
<div className="user-text">
  <div className="user-label">
    {loading ? "Loading user..." : "Logged in as"}
  </div>

  <div className="user-name">
    {loading
      ? "Loading..."
      : employee?.empName || "Employee name unavailable"}
  </div>

  <div className="user-name">
    {employee?.designation || ""}
  </div>
</div>

      </div>
<button
  type="button"
  className="logout-button"
  onClick={handleLogout}
>
  <svg
    width="16"
    height="16"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
  >
    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
    <polyline points="16 17 21 12 16 7" />
    <line x1="21" y1="12" x2="9" y2="12" />
  </svg>
  <span>Logout</span>
</button>
    </div>

  </div>
);
};

export default Header;