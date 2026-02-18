import React from 'react'

function AdminView({
  equipment,
  selectedEquipment,
  setSelectedEquipment,
  pendingRequests,
  queue,
  handleApprove,
  handleCallNext,
}) {
  const getFilteredPending = () => {
    if (!selectedEquipment) return []
    return pendingRequests.filter((req) => req.equipmentId === selectedEquipment.id)
  }

  return (
    <div className="fade-in">
      <div className="section-header">
        <h2>Health Logistics Control</h2>
        <p>Manage equipment triage and patient queue flow.</p>
      </div>

      <div className="admin-header-cards">
        {equipment.map((eq) => (
          <div
            key={eq.id}
            className={`card ${selectedEquipment?.id === eq.id ? 'selected' : ''}`}
            onClick={() => setSelectedEquipment(eq)}
          >
            <h4>{eq.name}</h4>
            <small>{eq.status}</small>
            <div className="eq-buffer-time">
              ⏱️ {eq.bufferTime >= 60 ? `${Math.floor(eq.bufferTime / 60)}h ${eq.bufferTime % 60}m` : `${eq.bufferTime} min`}
            </div>
          </div>
        ))}
      </div>

      <div className="grid-layout">
        <div className="section">
          <h3>🔔 Triage Queue {selectedEquipment && `for ${selectedEquipment.name}`}</h3>
          <div className="request-list">
            {!selectedEquipment ? (
              <p>Please select a machine above.</p>
            ) : getFilteredPending().length === 0 ? (
              <p>No pending triage requests.</p>
            ) : (
              getFilteredPending().map((req) => (
                <div key={req.id} className="item-card">
                  <div className="item-info">
                    <h4>{req.patientName}</h4>
                    <small>
                      Preferred:{' '}
                      {req.slotTime
                        ? req.slotTime.toString().replace('T', ' ')
                        : 'As soon as possible'}
                    </small>
                  </div>
                  <div className="action-btns">
                    <button
                      className="btn-icon"
                      onClick={() => handleApprove(req.id, 'NORMAL')}
                    >
                      Normal
                    </button>
                    <button
                      className="btn-icon"
                      onClick={() => handleApprove(req.id, 'URGENT')}
                    >
                      Urgent
                    </button>
                    <button
                      className="btn-icon btn-emergency"
                      onClick={() => handleApprove(req.id, 'EMERGENCY')}
                    >
                      EMERGENCY
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="section">
          <h3>📋 Live Operations {selectedEquipment && `(${selectedEquipment.name})`}</h3>
          {selectedEquipment ? (
            <>
              <button
                className="btn-primary"
                style={{ width: '100%', marginBottom: '1rem' }}
                onClick={handleCallNext}
              >
                Call Next Patient
              </button>
              <div className="queue-list">
                {queue.length === 0 ? (
                  <p>Operational queue empty.</p>
                ) : (
                  queue.map((q, idx) => (
                    <div key={q.id} className="item-card">
                      <span style={{ fontWeight: 'bold', width: '20px' }}>#{idx + 1}</span>
                      <div className="item-info">
                        <h4>{q.patientName}</h4>
                        <small>
                          {q.slotTime
                            ? q.slotTime.toString().replace('T', ' ')
                            : 'As soon as possible'}
                        </small>
                      </div>
                      <span className={`prio-tag prio-${q.priority}`}>{q.priority}</span>
                    </div>
                  ))
                )}
              </div>
            </>
          ) : (
            <p>Select a facility to view operations.</p>
          )}
        </div>
      </div>
    </div>
  )
}

export default AdminView

