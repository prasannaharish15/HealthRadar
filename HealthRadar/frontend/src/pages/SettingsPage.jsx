import { useState, useEffect } from 'react';
import { settingsAPI } from '../services/api';
import { MdSearch, MdNotificationsActive, MdLock, MdMap, MdEmail, MdSettings } from 'react-icons/md';

const CATEGORY_INFO = {
  ANOMALY_DETECTION: { icon: <MdSearch />, label: 'Anomaly Detection' },
  ALERTS: { icon: <MdNotificationsActive />, label: 'Alert Settings' },
  PRIVACY: { icon: <MdLock />, label: 'Privacy & Compliance' },
  VISUALIZATION: { icon: <MdMap />, label: 'Visualization' },
  NOTIFICATIONS: { icon: <MdEmail />, label: 'Notifications' },
};

export default function SettingsPage() {
  const [settings, setSettings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(null);

  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    try {
      const res = await settingsAPI.getAll();
      setSettings(res.data);
    } catch (err) {
      console.error('Failed to load settings:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async (key, value) => {
    setSaving(key);
    try {
      await settingsAPI.update(key, value);
      setSettings(prev => prev.map(s =>
        s.settingKey === key ? { ...s, settingValue: value } : s
      ));
    } catch (err) {
      console.error('Update failed:', err);
    } finally {
      setSaving(null);
    }
  };

  const groupedSettings = settings.reduce((acc, setting) => {
    const cat = setting.category || 'GENERAL';
    if (!acc[cat]) acc[cat] = [];
    acc[cat].push(setting);
    return acc;
  }, {});

  if (loading) return <div className="loader"><div className="spinner" /></div>;

  return (
    <div>
      <div className="page-header">
        <h1>Settings & Privacy</h1>
        <p>Configure anomaly detection thresholds, privacy rules, and notification preferences</p>
      </div>

      {Object.entries(groupedSettings).map(([category, items]) => {
        const info = CATEGORY_INFO[category] || { icon: <MdSettings />, label: category };
        return (
          <div key={category} className="settings-section">
            <h3>{info.icon} {info.label}</h3>
            {items.map((setting) => {
              const isBool = setting.settingValue === 'true' || setting.settingValue === 'false';

              return (
                <div key={setting.settingKey} className="setting-row">
                  <div className="setting-info">
                    <div className="label">{setting.settingKey.split('.').slice(-2).join(' ').replace(/\b\w/g, c => c.toUpperCase())}</div>
                    <div className="desc">{setting.description}</div>
                  </div>

                  {isBool ? (
                    <button
                      className={`toggle-switch ${setting.settingValue === 'true' ? 'active' : ''}`}
                      onClick={() => handleUpdate(setting.settingKey,
                        setting.settingValue === 'true' ? 'false' : 'true')}
                      disabled={saving === setting.settingKey}
                    />
                  ) : (
                    <input
                      className="form-input setting-input"
                      value={setting.settingValue}
                      onChange={(e) => {
                        setSettings(prev => prev.map(s =>
                          s.settingKey === setting.settingKey ? { ...s, settingValue: e.target.value } : s
                        ));
                      }}
                      onBlur={(e) => handleUpdate(setting.settingKey, e.target.value)}
                    />
                  )}
                </div>
              );
            })}
          </div>
        );
      })}
    </div>
  );
}
