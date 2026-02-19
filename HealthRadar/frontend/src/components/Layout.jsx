import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { MdDashboard, MdMap, MdNotificationsActive, MdLocalHospital, MdAdminPanelSettings, MdSettings, MdLogout } from 'react-icons/md';

export default function Layout() {
  const { user, logout, isAdmin } = useAuth();

  const navItems = [
    { to: '/dashboard', icon: <MdDashboard />, label: 'Dashboard' },
    { to: '/heatmap', icon: <MdMap />, label: 'Heatmap' },
    { to: '/alerts', icon: <MdNotificationsActive />, label: 'Alerts' },
    { to: '/clinics', icon: <MdLocalHospital />, label: 'Clinic Data' },
    ...(isAdmin() ? [{ to: '/admin', icon: <MdAdminPanelSettings />, label: 'Admin Panel' }] : []),
    { to: '/settings', icon: <MdSettings />, label: 'Settings' },
  ];

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="sidebar-logo">
          <h1>HealthTrend</h1>
          <span>Regional Dashboard</span>
        </div>

        <nav className="sidebar-nav">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
            >
              <span className="icon">{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div style={{ marginBottom: 12, fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            Signed in as <strong style={{ color: 'var(--text-primary)' }}>{user?.fullName}</strong>
            <br />
            <span style={{ fontSize: '0.7rem' }}>{user?.role}</span>
          </div>
          <button onClick={logout}><MdLogout style={{ marginRight: 6, verticalAlign: 'middle' }} />Sign Out</button>
        </div>
      </aside>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
