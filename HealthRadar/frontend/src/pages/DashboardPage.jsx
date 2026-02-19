import { useState, useEffect } from 'react';
import { dashboardAPI, alertAPI } from '../services/api';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';

const SEVERITY_COLORS = {
  LOW: '#06d6a0',
  MEDIUM: '#f59e0b',
  HIGH: '#f97316',
  CRITICAL: '#ef4444',
};

export default function DashboardPage() {
  const [stats, setStats] = useState(null);
  const [recentAlerts, setRecentAlerts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [statsRes, alertsRes] = await Promise.all([
        dashboardAPI.getStats(),
        dashboardAPI.getRecentAlerts(48),
      ]);
      setStats(statsRes.data);
      setRecentAlerts(alertsRes.data);
    } catch (err) {
      console.error('Failed to load dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="loader"><div className="spinner" /></div>;

  const severityData = stats?.alertsBySeverity
    ? Object.entries(stats.alertsBySeverity).map(([name, value]) => ({ name, value }))
    : [];

  return (
    <div>
      <div className="page-header">
        <h1>Dashboard</h1>
        <p>Overview of regional health trends and active alerts</p>
      </div>

      {/* Stats Grid */}
      <div className="stats-grid">
        <div className="stat-card blue">
          <div className="label">Total Clinics</div>
          <div className="value">{stats?.totalClinics || 0}</div>
        </div>
        <div className="stat-card purple">
          <div className="label">Total Submissions</div>
          <div className="value">{stats?.totalSubmissions || 0}</div>
        </div>
        <div className="stat-card orange">
          <div className="label">Pending Alerts</div>
          <div className="value">{stats?.pendingAlerts || 0}</div>
        </div>
        <div className="stat-card cyan">
          <div className="label">Acknowledged</div>
          <div className="value">{stats?.acknowledgedAlerts || 0}</div>
        </div>
        <div className="stat-card green">
          <div className="label">Resolved</div>
          <div className="value">{stats?.resolvedAlerts || 0}</div>
        </div>
        <div className="stat-card red">
          <div className="label">Critical Alerts</div>
          <div className="value">{stats?.criticalAlerts || 0}</div>
        </div>
      </div>

      <div className="grid-2">
        {/* Severity Distribution */}
        <div className="chart-container">
          <div className="card-header">
            <span className="card-title">Alert Severity Distribution</span>
          </div>
          {severityData.length > 0 ? (
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie data={severityData} dataKey="value" nameKey="name" cx="50%" cy="50%"
                     outerRadius={100} innerRadius={50} paddingAngle={3} label={({ name, value }) => `${name}: ${value}`}>
                  {severityData.map((entry) => (
                    <Cell key={entry.name} fill={SEVERITY_COLORS[entry.name] || '#4f6ef7'} />
                  ))}
                </Pie>
                <Tooltip contentStyle={{ background: '#1e2130', border: '1px solid #2a2d3e', borderRadius: 8, color: '#e8eaf0' }} />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="text-center text-muted" style={{ padding: 60 }}>No alert data available</div>
          )}
        </div>

        {/* Recent Alerts */}
        <div className="chart-container">
          <div className="card-header">
            <span className="card-title">Recent Alerts (48h)</span>
            <span className="badge badge-pending">{recentAlerts.length} total</span>
          </div>
          <div style={{ maxHeight: 300, overflowY: 'auto' }}>
            {recentAlerts.length > 0 ? recentAlerts.slice(0, 8).map((alert) => (
              <div key={alert.id} className="alert-item" style={{ marginBottom: 8, padding: 14 }}>
                <div className="alert-item-header">
                  <span className="alert-item-title">{alert.symptomCategory}</span>
                  <span className={`badge badge-${alert.severity?.toLowerCase()}`}>{alert.severity}</span>
                </div>
                <div className="alert-item-meta">
                  <span>Clinic: {alert.clinic?.name || 'N/A'}</span>
                  <span className={`badge badge-${alert.status?.toLowerCase()}`}>{alert.status}</span>
                </div>
              </div>
            )) : (
              <div className="text-center text-muted" style={{ padding: 60 }}>No recent alerts</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
