import React from "react";
import "./Sidebar.css";

const Sidebar = ({ menus, activePage, onMenuClick }) => {
  return (
    <div className="sidebar">

      {/* HEADER */}
      <div className="sidebar-header">

        <div className="sidebar-logo">
          GPF
        </div>

        <div>
          <div className="sidebar-title">
            GPF Portal
          </div>

          <div className="sidebar-subtitle">
            Office Automation Division
          </div>
        </div>

      </div>

      {/* SECTION */}
      

      {/* MENU */}
      <ul className="menu-list">

        {menus.map((menu) => (

          <li
            key={menu.menuId}
            className={
              activePage === menu.menuName
                ? "menu-item active"
                : "menu-item"
            }
            onClick={() => onMenuClick(menu.menuName)}
          >

            <span className="menu-icon">
              •
            </span>

            <span className="menu-text">
              {menu.menuName}
            </span>

          </li>

        ))}

      </ul>

      {/* FOOTER */}
      <div className="sidebar-footer">
        GPF Management System v1.0
      </div>

    </div>
  );
};

export default Sidebar;