import React from 'react'

function PatientView({
  equipment,
  selectedEquipment,
  setSelectedEquipment,
  patientName,
  setPatientName,
  slotTime,
  setSlotTime,
  requestedPriority,
  setRequestedPriority,
  handleRequestBooking,
}) {
  return (
    <div className="fade-in">
      <div className="section-header">
        <h2>Patient Service Portal</h2>
        <p>Select a facility and request your scheduled scan or procedure.</p>
      </div>

      <div className="grid-layout">
        <div className="section">
          <h3>Available Facilities</h3>
          <div className="eq-grid">
            {equipment.length === 0 ? (
              <p>Loading facilities... (Check if backend is on 8080)</p>
            ) : (
              equipment.map((eq) => (
                <div
                  key={eq.id}
                  className={`eq-card ${
                    selectedEquipment?.id === eq.id ? 'selected' : ''
                  }`}
                  onClick={() => setSelectedEquipment(eq)}
                >
                  {/* Safely handle cases where status might be null */}
                  <span
                    className={`status-indicator status-${(
                      eq.status || 'UNKNOWN'
                    ).toLowerCase()}`}
                  >
                    {(eq.status || 'UNKNOWN').toString().replace('_', ' ')}
                  </span>
                  <h3>{eq.name}</h3>
                  <div className="eq-type">{eq.type}</div>
                  <div className="eq-meta">
                    <span>
                      ⏳ Next: <strong>{eq.nextAvailable}</strong>
                    </span>
                    <span>
                      👥 Wait: <strong>{eq.queueLength}</strong>
                    </span>
                  </div>
                  <div className="eq-duration">
                    <span className="duration-icon">⏱️</span>
                    <span className="duration-text">
                      Procedure: <strong>{eq.bufferTime >= 60 ? `${Math.floor(eq.bufferTime / 60)}h ${eq.bufferTime % 60}m` : `${eq.bufferTime} min`}</strong>
                    </span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="section">
          <div className="booking-panel">
            <h3>Request a Booking</h3>
            {selectedEquipment && (
              <div className="selected-eq-info">
                <div className="eq-info-item">
                  <span className="eq-info-label">Selected:</span>
                  <span className="eq-info-value">{selectedEquipment.name}</span>
                </div>
                <div className="eq-info-item">
                  <span className="eq-info-label">Procedure Time:</span>
                  <span className="eq-info-value highlight">
                    {selectedEquipment.bufferTime >= 60 
                      ? `${Math.floor(selectedEquipment.bufferTime / 60)}h ${selectedEquipment.bufferTime % 60}m` 
                      : `${selectedEquipment.bufferTime} min`}
                  </span>
                </div>
              </div>
            )}
            <form onSubmit={handleRequestBooking}>
              <div className="form-group">
                <label>Patient Full Name</label>
                <input
                  type="text"
                  value={patientName}
                  onChange={(e) => setPatientName(e.target.value)}
                  placeholder="e.g. John Doe"
                  required
                />
              </div>
              <div className="form-group">
                <label>Preferred Date & Time</label>
                <input
                  type="datetime-local"
                  value={slotTime}
                  onChange={(e) => setSlotTime(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label>Service Urgency</label>
                <select
                  value={requestedPriority}
                  onChange={(e) => setRequestedPriority(e.target.value)}
                >
                  <option value="NORMAL">Standard Checkup</option>
                  <option value="URGENT">Urgent Care Needed</option>
                  <option value="EMERGENCY">🚨 Critical Emergency</option>
                </select>
              </div>
              <button
                type="submit"
                className="btn-primary"
                style={{ width: '100%' }}
                disabled={!selectedEquipment}
              >
                {selectedEquipment ? `Book ${selectedEquipment.name}` : 'Select a Facility'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}

export default PatientView

