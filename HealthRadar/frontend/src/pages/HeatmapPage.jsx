import { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Circle, Popup } from 'react-leaflet';
import { vizAPI } from '../services/api';
import { MdWarning } from 'react-icons/md';

const INTENSITY_COLORS = {
  LOW: '#06d6a0',
  MEDIUM: '#f59e0b',
  HIGH: '#f97316',
  CRITICAL: '#ef4444',
};

export default function HeatmapPage() {
  const [heatmapData, setHeatmapData] = useState(null);
  const [selectedZone, setSelectedZone] = useState(null);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadHeatmap();
  }, []);

  const loadHeatmap = async () => {
    setLoading(true);
    try {
      const res = await vizAPI.getHeatmap(startDate || null, endDate || null);
      setHeatmapData(res.data);
    } catch (err) {
      console.error('Failed to load heatmap:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDrilldown = async (clinicId) => {
    try {
      const res = await vizAPI.getClinicDrilldown(clinicId, startDate || null, endDate || null);
      if (res.data.zones?.length > 0) {
        setSelectedZone(res.data.zones[0]);
      }
    } catch (err) {
      console.error('Drill-down failed:', err);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1>Interactive Heatmap</h1>
        <p>Geographic visualization of health trends with ~10km zone radius</p>
      </div>

      {/* Controls */}
      <div className="heatmap-controls">
        <div className="form-group" style={{ marginBottom: 0 }}>
          <label>Start Date</label>
          <input type="date" className="form-input" value={startDate}
            onChange={(e) => setStartDate(e.target.value)} />
        </div>
        <div className="form-group" style={{ marginBottom: 0 }}>
          <label>End Date</label>
          <input type="date" className="form-input" value={endDate}
            onChange={(e) => setEndDate(e.target.value)} />
        </div>
        <button className="btn btn-primary" onClick={loadHeatmap}
          style={{ alignSelf: 'flex-end' }}>
          Apply Filter
        </button>
      </div>

      {/* Map */}
      {loading ? (
        <div className="loader"><div className="spinner" /></div>
      ) : (
        <div className="heatmap-container">
          <MapContainer
            center={[12.5, 79.5]}
            zoom={7}
            style={{ height: 500 }}
            scrollWheelZoom={true}
          >
            <TileLayer
              url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
              attribution='&copy; <a href="https://carto.com/">CARTO</a>'
            />
            {heatmapData?.zones?.map((zone) => (
              <Circle
                key={zone.clinicId}
                center={[parseFloat(zone.latitude), parseFloat(zone.longitude)]}
                radius={(zone.radiusKm || 10) * 1000}
                pathOptions={{
                  color: INTENSITY_COLORS[zone.intensityLevel] || '#4f6ef7',
                  fillColor: INTENSITY_COLORS[zone.intensityLevel] || '#4f6ef7',
                  fillOpacity: 0.3,
                  weight: 2,
                }}
                eventHandlers={{
                  click: () => handleDrilldown(zone.clinicId),
                }}
              >
                <Popup>
                  <div style={{ color: '#1a1d27', minWidth: 200 }}>
                    <strong style={{ fontSize: '1rem' }}>{zone.clinicName}</strong>
                    <p style={{ margin: '4px 0', fontSize: '0.85rem' }}>Region: {zone.region}</p>
                    <p style={{ margin: '4px 0', fontSize: '0.85rem' }}>Total Cases: <strong>{zone.totalCases}</strong></p>
                    <p style={{ margin: '4px 0', fontSize: '0.85rem' }}>Dominant: {zone.dominantSymptom}</p>
                    <p style={{ margin: '4px 0', fontSize: '0.85rem' }}>Intensity: <strong>{zone.intensityLevel}</strong></p>
                    {zone.symptoms?.map((s) => (
                      <div key={s.category} style={{ fontSize: '0.8rem', display: 'flex', justifyContent: 'space-between' }}>
                        <span>{s.category}</span>
                        <span style={{ color: s.isAnomaly ? '#ef4444' : '#333', fontWeight: s.isAnomaly ? 700 : 400 }}>
                          {s.count} {s.isAnomaly ? <MdWarning style={{ color: '#f59e0b', verticalAlign: 'middle' }} /> : ''}
                        </span>
                      </div>
                    ))}
                  </div>
                </Popup>
              </Circle>
            ))}
          </MapContainer>
        </div>
      )}

      {/* Legend */}
      <div className="heatmap-legend">
        {Object.entries(INTENSITY_COLORS).map(([level, color]) => (
          <div key={level} className="legend-item">
            <div className="legend-dot" style={{ backgroundColor: color }} />
            {level}
          </div>
        ))}
      </div>

      {/* Drill-down Panel */}
      {selectedZone && (
        <div className="card mt-4">
          <div className="card-header">
            <span className="card-title">Drill-down: {selectedZone.clinicName}</span>
            <button className="btn btn-sm btn-secondary" onClick={() => setSelectedZone(null)}>Close</button>
          </div>
          <div className="stats-grid" style={{ marginBottom: 16 }}>
            <div className="stat-card blue">
              <div className="label">Total Cases</div>
              <div className="value">{selectedZone.totalCases}</div>
            </div>
            <div className="stat-card orange">
              <div className="label">Dominant Symptom</div>
              <div className="value" style={{ fontSize: '1.2rem' }}>{selectedZone.dominantSymptom}</div>
            </div>
          </div>
          <div className="table-wrapper">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Symptom Category</th>
                  <th>Count</th>
                  <th>Anomaly</th>
                </tr>
              </thead>
              <tbody>
                {selectedZone.symptoms?.map((s) => (
                  <tr key={s.category}>
                    <td>{s.category}</td>
                    <td>{s.count}</td>
                    <td>{s.isAnomaly ? <span className="badge badge-critical" style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}><MdWarning /> Anomaly</span> : <span className="text-muted">Normal</span>}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
