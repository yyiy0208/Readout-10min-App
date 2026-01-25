import React, { useState, useEffect } from 'react';

const Navbar: React.FC = () => {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  const toggleDropdown = () => {
    setIsDropdownOpen(!isDropdownOpen);
  };

  const handleLogout = () => {
    alert('退出成功');
    setIsDropdownOpen(false);
  };

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (!event.target || !(event.target as HTMLElement).closest('.dropdown')) {
        setIsDropdownOpen(false);
      }
    };

    document.addEventListener('click', handleClickOutside);
    return () => {
      document.removeEventListener('click', handleClickOutside);
    };
  }, []);

  return (
    <header className="top-nav">
      <div className="logo">Readout-10min</div>
      <nav className="nav-links">
        <a href="#" className="active">文件管理</a>
        <a href="#">文件处理</a>
        <div className="dropdown">
          <a href="#" className="dropdown-toggle" onClick={toggleDropdown}>
            账号管理
          </a>
          <div className={`dropdown-menu ${isDropdownOpen ? 'show' : ''}`} id="account-menu">
            <button className="dropdown-item" onClick={handleLogout}>
              退出
            </button>
          </div>
        </div>
      </nav>
    </header>
  );
};

export default Navbar;