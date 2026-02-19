import { useState, useEffect } from 'react';
import { adminAPI } from '../services/api';

export default function AdminPanel() {
  const [users, setUsers] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [activeTab, setActiveTab] = useState('users');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [usersRes, logsRes] = await Promise.all([
        adminAPI.getUsers(),
        adminAPI.getAuditLogs(),
      ]);
      setUsers(usersRes.data);
      setAuditLogs(logsRes.data);
    } catch (err) {
      console.error('Failed to load admin data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRoleChange = async (userId, role) => {
    try {
      await adminAPI.updateRole(userId, role);
      loadData();
    } catch (err) {
      console.error('Role update failed:', err);
    }
  };

  const handleToggleStatus = async (userId) => {
    try {
      await adminAPI.toggleStatus(userId);
      loadData();
    } catch (err) {
      console.error('Status toggle failed:', err);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleString();
  };

  if (loading) return <div className="loader"><div className="spinner" /></div>;

  return (
    <div>
      <div className="page-header">
        <h1>Admin Panel</h1>
        <p>Manage users, review audit logs, and system health</p>
      </div>

      {/* Stats */}
      <div className="stats-grid">
        <div className="stat-card blue">
          <div className="label">Total Users</div>
          <div className="value">{users.length}</div>
        </div>
        <div className="stat-card green">
          <div className="label">Active Users</div>
          <div className="value">{users.filter(u => u.isActive).length}</div>
        </div>
        <div className="stat-card purple">
          <div className="label">Audit Events</div>
          <div className="value">{auditLogs.length}</div>
        </div>
        <div className="stat-card orange">
          <div className="label">Admin Users</div>
          <div className="value">{users.filter(u => u.role === 'ADMIN').length}</div>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="alert-filters mb-4">
        <button className={`filter-btn ${activeTab === 'users' ? 'active' : ''}`}
                onClick={() => setActiveTab('users')}>User Management</button>
        <button className={`filter-btn ${activeTab === 'audit' ? 'active' : ''}`}
                onClick={() => setActiveTab('audit')}>Audit Logs</button>
      </div>

      {/* Users Tab */}
      {activeTab === 'users' && (
        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Username</th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>Last Login</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td><strong>{user.username}</strong></td>
                  <td>{user.fullName}</td>
                  <td>{user.email}</td>
                  <td>
                    <select className="form-input" style={{ width: 120, padding: '6px 10px', fontSize: '0.8rem' }}
                            value={user.role}
                            onChange={(e) => handleRoleChange(user.id, e.target.value)}>
                      <option value="ADMIN">Admin</option>
                      <option value="ANALYST">Analyst</option>
                      <option value="VIEWER">Viewer</option>
                    </select>
                  </td>
                  <td>
                    <span className={`badge ${user.isActive ? 'badge-resolved' : 'badge-critical'}`}>
                      {user.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="text-muted">{formatDate(user.lastLogin)}</td>
                  <td>
                    <button className={`btn btn-sm ${user.isActive ? 'btn-danger' : 'btn-success'}`}
                            onClick={() => handleToggleStatus(user.id)}>
                      {user.isActive ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Audit Logs Tab */}
      {activeTab === 'audit' && (
        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>User</th>
                <th>Action</th>
                <th>Entity</th>
                <th>Details</th>
              </tr>
            </thead>
            <tbody>
              {auditLogs.slice(0, 50).map((log) => (
                <tr key={log.id}>
                  <td className="text-muted">{formatDate(log.createdAt)}</td>
                  <td>{log.user?.fullName || 'System'}</td>
                  <td><span className="badge badge-acknowledged">{log.action}</span></td>
                  <td>{log.entityType} #{log.entityId}</td>
                  <td className="text-muted" style={{ maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {log.details ? JSON.parse(log.details)?.message : '—'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
