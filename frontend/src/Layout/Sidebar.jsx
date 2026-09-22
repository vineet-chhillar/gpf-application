import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Sidebar.css";

const Sidebar = ({ menus, activePage, onMenuClick }) => {
  const [expandedMenus, setExpandedMenus] = useState({
    100: true, // GPF Workflow Master
    200: true, // Apply For GPF
  });
const navigate = useNavigate();
  const toggleMenu = (menuId) => {
    setExpandedMenus((prev) => ({
      ...prev,
      [menuId]: !prev[menuId],
    }));
  };
const handleMenuClick = (menuName) => {

  {/*setActivePage(menuName);*/}

  if (menuName === "Pending For Action") {
    navigate("/workflow");
    return;
  }

  navigate("/");
};
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

      {/* MENU */}
      <ul className="menu-list">
        {menus.map((menu) => (
          <React.Fragment key={menu.menuId}>
            {menu.children ? (
              <>
                <li
                  className="menu-item menu-parent"
                  onClick={() => toggleMenu(menu.menuId)}
                >
                  <span className="menu-icon">
                    {expandedMenus[menu.menuId] ? "▼" : "▶"}
                  </span>

                  <span className="menu-text">
                    {menu.menuName}
                  </span>
                </li>

                {expandedMenus[menu.menuId] &&
                  menu.children.map((child) => (
                    <li
                      key={child.menuId}
                      className={
                        activePage === child.menuName
                          ? "menu-item child-menu active"
                          : "menu-item child-menu"
                      }
                      onClick={() => onMenuClick(child.menuName)}
                    >
                      <span className="menu-icon">
                        •
                      </span>

                      <span className="menu-text">
                        {child.menuName}
                      </span>
                    </li>
                  ))}
              </>
            ) : (
              <li
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
            )}
          </React.Fragment>
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