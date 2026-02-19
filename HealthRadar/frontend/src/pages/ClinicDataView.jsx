import { useState, useEffect } from 'react';
import { clinicAPI, submissionAPI } from '../services/api';

export default function ClinicDataView() {
  const [clinics, setClinics] = useState([]);
  const [selectedClinic, setSelectedClinic] = useState(null);
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showSubmitForm, setShowSubmitForm] = useState(false);
  const [formData, setFormData] = useState({
    clinicId: '', submissionDate: '', symptomCategory: '', caseCount: '', groupSize: '', notes: ''
  });
  const [submitError, setSubmitError] = useState('');

  useEffect(() => {
    loadClinics();
  }, []);

  const loadClinics = async () => {
    try {
      const res = await clinicAPI.getAll();
      setClinics(res.data);
    } catch (err) {
      console.error('Failed to load clinics:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadSubmissions = async (clinicId) => {
    try {
      const res = await submissionAPI.getByClinic(clinicId);
      setSubmissions(res.data);
    } catch (err) {
      console.error('Failed to load submissions:', err);
    }
  };

  const handleClinicSelect = (clinic) => {
    setSelectedClinic(clinic);
    loadSubmissions(clinic.id);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitError('');
    try {
      await submissionAPI.create({
        clinicId: parseInt(formData.clinicId),
        submissionDate: formData.submissionDate,
        symptomCategory: formData.symptomCategory,
        caseCount: parseInt(formData.caseCount),
        groupSize: parseInt(formData.groupSize),
        notes: formData.notes,
      });
      setShowSubmitForm(false);
      setFormData({ clinicId: '', submissionDate: '', symptomCategory: '', caseCount: '', groupSize: '', notes: '' });
      if (selectedClinic) loadSubmissions(selectedClinic.id);
    } catch (err) {
      setSubmitError(err.response?.data?.error || 'Submission failed');
    }
  };

  if (loading) return <div className="loader"><div className="spinner" /></div>;

  return (
    <div>
      <div className="page-header flex justify-between items-center">
        <div>
          <h1>Clinic Data</h1>
          <p>View clinic submissions and submit new data</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowSubmitForm(!showSubmitForm)}>
          {showSubmitForm ? 'Cancel' : '+ New Submission'}
        </button>
      </div>

      {/* Submission Form */}
      {showSubmitForm && (
        <div className="card mb-4">
          <h3 style={{ marginBottom: 16 }}>Submit Clinic Data</h3>
          {submitError && <div className="error-msg" style={{
            background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)',
            borderRadius: 8, padding: '10px 14px', color: '#ef4444', fontSize: '0.85rem', marginBottom: 16
          }}>{submitError}</div>}
          <form onSubmit={handleSubmit}>
            <div className="grid-2">
              <div className="form-group">
                <label>Clinic</label>
                <select className="form-input" value={formData.clinicId}
                        onChange={(e) => setFormData({...formData, clinicId: e.target.value})} required>
                  <option value="">Select clinic...</option>
                  {clinics.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Submission Date</label>
                <input type="date" className="form-input" value={formData.submissionDate}
                       onChange={(e) => setFormData({...formData, submissionDate: e.target.value})} required />
              </div>
              <div className="form-group">
                <label>Symptom Category</label>
                <input type="text" className="form-input" placeholder="e.g. Respiratory, Fever"
                       value={formData.symptomCategory}
                       onChange={(e) => setFormData({...formData, symptomCategory: e.target.value})} required />
              </div>
              <div className="form-group">
                <label>Case Count</label>
                <input type="number" className="form-input" min="0" value={formData.caseCount}
                       onChange={(e) => setFormData({...formData, caseCount: e.target.value})} required />
              </div>
              <div className="form-group">
                <label>Group Size (min 5 for privacy)</label>
                <input type="number" className="form-input" min="0" value={formData.groupSize}
                       onChange={(e) => setFormData({...formData, groupSize: e.target.value})} required />
              </div>
              <div className="form-group">
                <label>Notes</label>
                <input type="text" className="form-input" placeholder="Optional notes"
                       value={formData.notes}
                       onChange={(e) => setFormData({...formData, notes: e.target.value})} />
              </div>
            </div>
            <button type="submit" className="btn btn-primary">Submit Data</button>
          </form>
        </div>
      )}

      {/* Clinics Table */}
      <div className="table-wrapper mb-4">
        <table className="data-table">
          <thead>
            <tr>
              <th>Code</th>
              <th>Name</th>
              <th>Region</th>
              <th>District</th>
              <th>Coordinates</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {clinics.map((clinic) => (
              <tr key={clinic.id} style={{ cursor: 'pointer' }}
                  onClick={() => handleClinicSelect(clinic)}>
                <td><strong>{clinic.code}</strong></td>
                <td>{clinic.name}</td>
                <td>{clinic.region}</td>
                <td>{clinic.district}</td>
                <td>{parseFloat(clinic.latitude).toFixed(4)}, {parseFloat(clinic.longitude).toFixed(4)}</td>
                <td><span className={`badge ${clinic.isActive ? 'badge-resolved' : 'badge-pending'}`}>
                  {clinic.isActive ? 'Active' : 'Inactive'}</span></td>
                <td><button className="btn btn-sm btn-secondary">View</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Submissions for Selected Clinic */}
      {selectedClinic && (
        <div className="card">
          <div className="card-header">
            <span className="card-title">Submissions — {selectedClinic.name}</span>
            <button className="btn btn-sm btn-secondary" onClick={() => { setSelectedClinic(null); setSubmissions([]); }}>Close</button>
          </div>
          {submissions.length > 0 ? (
            <div className="table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Symptom</th>
                    <th>Cases</th>
                    <th>Group Size</th>
                    <th>Valid</th>
                    <th>Notes</th>
                  </tr>
                </thead>
                <tbody>
                  {submissions.map((s) => (
                    <tr key={s.id}>
                      <td>{s.submissionDate}</td>
                      <td>{s.symptomCategory}</td>
                      <td><strong>{s.caseCount}</strong></td>
                      <td>{s.groupSize}</td>
                      <td><span className={`badge ${s.isValid ? 'badge-resolved' : 'badge-critical'}`}>
                        {s.isValid ? 'Valid' : 'Invalid'}</span></td>
                      <td className="text-muted">{s.notes || '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-center text-muted" style={{ padding: 40 }}>No submissions for this clinic</p>
          )}
        </div>
      )}
    </div>
  );
}
