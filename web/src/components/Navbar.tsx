import React, { useState, useEffect } from 'react';
import { useLocation, Link } from 'react-router-dom';

const Navbar: React.FC = () => {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const location = useLocation();

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
        <Link 
          to="/" 
          className={location.pathname === '/' ? 'active' : ''}
        >
          文件管理
        </Link>
        <Link 
          to="/file-processing" 
          className={location.pathname === '/file-processing' ? 'active' : ''}
        >
          文件处理
        </Link>
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