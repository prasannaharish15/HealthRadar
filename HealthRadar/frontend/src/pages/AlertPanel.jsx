import { useState, useEffect } from 'react';
import { alertAPI } from '../services/api';
import { MdPriorityHigh, MdCheckCircle, MdDone } from 'react-icons/md';

export default function AlertPanel() {
  const [alerts, setAlerts] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [resolveModal, setResolveModal] = useState(null);
  const [resolutionNotes, setResolutionNotes] = useState('');

  useEffect(() => {
    loadAlerts();
  }, [filter]);

  const loadAlerts = async () => {
    setLoading(true);
    try {
      const res = filter === 'ALL'
        ? await alertAPI.getAll()
        : await alertAPI.getByStatus(filter);
      setAlerts(res.data);
    } catch (err) {
      console.error('Failed to load alerts:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAcknowledge = async (id) => {
    try {
      await alertAPI.acknowledge(id);
      loadAlerts();
    } catch (err) {
      console.error('Acknowledge failed:', err);
    }
  };

  const handleResolve = async () => {
    if (!resolveModal) return;
    try {
      await alertAPI.resolve(resolveModal, resolutionNotes);
      setResolveModal(null);
      setResolutionNotes('');
      loadAlerts();
    } catch (err) {
      console.error('Resolve failed:', err);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleString();
  };

  return (
    <div>
      <div className="page-header">
        <h1>Alert Panel</h1>
        <p>Monitor, acknowledge, and resolve health anomaly alerts</p>
      </div>

      {/* Filters */}
      <div className="alert-filters">
        {['ALL', 'PENDING', 'ACKNOWLEDGED', 'RESOLVED'].map((f) => (
          <button
            key={f}
            className={`filter-btn ${filter === f ? 'active' : ''}`}
            onClick={() => setFilter(f)}
          >
            {f.charAt(0) + f.slice(1).toLowerCase()} {filter === f && `(${alerts.length})`}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="loader"><div className="spinner" /></div>
      ) : alerts.length === 0 ? (
        <div className="card text-center" style={{ padding: 60 }}>
          <p className="text-muted">No alerts found for this filter</p>
        </div>
      ) : (
        alerts.map((alert) => (
          <div key={alert.id} className="alert-item">
            <div className="alert-item-header">
              <div>
                <span className="alert-item-title">
                  {alert.symptomCategory}
                  {alert.isEscalated && <span style={{ color: 'var(--accent-red)', marginLeft: 8, display: 'inline-flex', alignItems: 'center', gap: 4 }}><MdPriorityHigh /> ESCALATED</span>}
                </span>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: 4 }}>
                  {alert.clinic?.name || 'Unknown Clinic'} • {alert.alertType?.replace('_', ' ')}
                </div>
              </div>
              <div className="flex gap-2">
                <span className={`badge badge-${alert.severity?.toLowerCase()}`}>{alert.severity}</span>
                <span className={`badge badge-${alert.status?.toLowerCase()}`}>{alert.status}</span>
              </div>
            </div>

            <div className="alert-item-meta">
              <span>Observed: <strong>{alert.observedValue}</strong></span>
              <span>Baseline: <strong>{alert.baselineValue}</strong></span>
              <span>Deviation: <strong>{alert.deviationFactor}x</strong></span>
              <span>Created: {formatDate(alert.createdAt)}</span>
            </div>

            {alert.description && (
              <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: 8 }}>
                {alert.description}
              </p>
            )}

            <div className="alert-item-actions">
              {alert.status === 'PENDING' && (
                <button className="btn btn-sm btn-warning" onClick={() => handleAcknowledge(alert.id)}>
                  <MdDone style={{ verticalAlign: 'middle', marginRight: 4 }} /> Acknowledge
                </button>
              )}
              {(alert.status === 'PENDING' || alert.status === 'ACKNOWLEDGED') && (
                <button className="btn btn-sm btn-success" onClick={() => setResolveModal(alert.id)}>
                  <MdCheckCircle style={{ verticalAlign: 'middle', marginRight: 4 }} /> Resolve
                </button>
              )}
            </div>

            {alert.acknowledgedBy && (
              <div className="text-sm text-muted mt-2">
                Acknowledged by {alert.acknowledgedBy.fullName} at {formatDate(alert.acknowledgedAt)}
              </div>
            )}
            {alert.resolvedBy && (
              <div className="text-sm text-muted mt-2">
                Resolved by {alert.resolvedBy.fullName} at {formatDate(alert.resolvedAt)}
                {alert.resolutionNotes && <> — {alert.resolutionNotes}</>}
              </div>
            )}
          </div>
        ))
      )}

      {/* Resolve Modal */}
      {resolveModal && (
        <div className="modal-overlay" onClick={() => setResolveModal(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>Resolve Alert</h2>
            <div className="form-group">
              <label>Resolution Notes</label>
              <textarea
                className="form-input"
                rows={4}
                placeholder="Describe the resolution..."
                value={resolutionNotes}
                onChange={(e) => setResolutionNotes(e.target.value)}
              />
            </div>
            <div className="flex gap-2" style={{ justifyContent: 'flex-end' }}>
              <button className="btn btn-secondary" onClick={() => setResolveModal(null)}>Cancel</button>
              <button className="btn btn-success" onClick={handleResolve}>Resolve Alert</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
