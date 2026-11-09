
import React, { useState } from "react";
import "./Sidebar.css";

const menuIcons = {
  "Home": "⌂",
  "Dashboard": "▦",
  "GPF Rule Master": "⚙",
  "GPF Workflow Master": "⇄",
  "WorkFlow HQ": "⌘",
  "WorkFlow States": "☷",
  "Apply For GPF": "▣",
  "WithDrawl": "↗",
  "Advance": "₹",
  "Track Status": "◷",
  "Inbox": "▤"
};

const Sidebar = ({
  menus,
  activePage,
  onMenuClick,
  collapsed
}) => {

  const [expandedMenus, setExpandedMenus] = useState({
    100: true,
    200: true
  });

  const toggleMenu = (menuId) => {
    setExpandedMenus(prev => ({
      ...prev,
      [menuId]: !prev[menuId]
    }));
  };

  const getIcon = (name) => menuIcons[name] || "•";

  return (
    <div className={`sidebar ${collapsed ? "collapsed" : ""}`}>

      {/* MENU */}
      <ul className="menu-list">

        {menus.map((menu) => (

          <React.Fragment key={menu.menuId}>

            {/* PARENT MENU */}
            {menu.children ? (

              <>
                <li
                  className={`menu-item menu-parent ${
                    menu.children.some(c => c.menuName === activePage)
                      ? "parent-active"
                      : ""
                  }`}
                  onClick={() => toggleMenu(menu.menuId)}
                  title={collapsed ? menu.menuName : ""}
                >

                  <span className="menu-icon">
                    {getIcon(menu.menuName)}
                  </span>

                  <span className="menu-text">
                    {menu.menuName}
                  </span>

                  <span className="menu-chevron">
                    {expandedMenus[menu.menuId] ? "⌄" : "›"}
                  </span>

                </li>

                {/* CHILD MENUS */}
                {expandedMenus[menu.menuId] &&
                  menu.children.map(child => (

                    <li
                      key={child.menuId}
                      className={`menu-item child-menu ${
                        activePage === child.menuName ? "active" : ""
                      }`}
                      onClick={() => onMenuClick(child.menuName)}
                      title={collapsed ? child.menuName : ""}
                    >

                      <span className="menu-icon">
                        {getIcon(child.menuName)}
                      </span>

                      <span className="menu-text">
                        {child.menuName}
                      </span>

                    </li>

                  ))}
              </>

            ) : (

              /* NORMAL MENU */
              <li
                className={`menu-item ${
                  activePage === menu.menuName ? "active" : ""
                }`}
                onClick={() => onMenuClick(menu.menuName)}
                title={collapsed ? menu.menuName : ""}
              >

                <span className="menu-icon">
                  {getIcon(menu.menuName)}
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
      {!collapsed && (
        <div className="sidebar-footer">
          GPF Management System v1.0
        </div>
      )}

    </div>
  );
};

export default Sidebar;
