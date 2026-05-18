import React from "react";
import "./Sidebar.css";

const Sidebar = ({ menus, activePage, onMenuClick }) => {
  return (
    <div className="sidebar">
      <div className="sidebar-header">
        <span className="sidebar-header-icon">📂</span>
        <span className="sidebar-title">MENU</span>
      </div>

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
            <span className="menu-icon">▸</span>
            <span className="menu-text">{menu.menuName}</span>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default Sidebar;
