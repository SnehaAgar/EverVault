import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import AdminView from '../AdminView'

describe('AdminView Component', () => {
  const mockProps = {
    equipment: [
      { id: 1, name: 'MRI-1', status: 'AVAILABLE' },
      { id: 2, name: 'CT-Scanner', status: 'IN_USE' },
    ],
    selectedEquipment: null,
    setSelectedEquipment: jest.fn(),
    pendingRequests: [
      { id: 1, patientName: 'John Doe', equipmentId: 1, slotTime: '2026-02-15T10:00' },
      { id: 2, patientName: 'Jane Smith', equipmentId: 1, slotTime: '2026-02-15T11:00' },
      { id: 3, patientName: 'Bob Wilson', equipmentId: 2, slotTime: '2026-02-15T12:00' },
    ],
    queue: [
      { id: 4, patientName: 'Alice Brown', priority: 'EMERGENCY', slotTime: '2026-02-15T09:00' },
      { id: 5, patientName: 'Charlie Davis', priority: 'NORMAL', slotTime: '2026-02-15T10:30' },
    ],
    handleApprove: jest.fn(),
    handleCallNext: jest.fn(),
  }

  beforeEach(() => {
    jest.clearAllMocks()
  })

  test('renders section header', () => {
    render(<AdminView {...mockProps} />)
    expect(screen.getByText('Health Logistics Control')).toBeInTheDocument()
    expect(screen.getByText(/Manage equipment triage/)).toBeInTheDocument()
  })

  test('renders equipment cards', () => {
    render(<AdminView {...mockProps} />)
    expect(screen.getByText('MRI-1')).toBeInTheDocument()
    expect(screen.getByText('CT-Scanner')).toBeInTheDocument()
  })

  test('clicking equipment card selects it', () => {
    render(<AdminView {...mockProps} />)
    const mriCard = screen.getByText('MRI-1').closest('.card')
    fireEvent.click(mriCard)
    expect(mockProps.setSelectedEquipment).toHaveBeenCalledWith(mockProps.equipment[0])
  })

  test('shows message when no equipment selected for triage', () => {
    render(<AdminView {...mockProps} />)
    expect(screen.getByText('Please select a machine above.')).toBeInTheDocument()
  })

  test('shows message when no equipment selected for operations', () => {
    render(<AdminView {...mockProps} />)
    expect(screen.getByText('Select a facility to view operations.')).toBeInTheDocument()
  })

  test('displays pending requests for selected equipment', () => {
    render(<AdminView {...mockProps} selectedEquipment={mockProps.equipment[0]} />)
    expect(screen.getByText('John Doe')).toBeInTheDocument()
    expect(screen.getByText('Jane Smith')).toBeInTheDocument()
    expect(screen.queryByText('Bob Wilson')).not.toBeInTheDocument() // Different equipment
  })

  test('shows no pending message when empty', () => {
    render(<AdminView {...mockProps} selectedEquipment={mockProps.equipment[0]} pendingRequests={[]} />)
    expect(screen.getByText('No pending triage requests.')).toBeInTheDocument()
  })

  test('approve buttons are present for each pending request', () => {
    render(<AdminView {...mockProps} selectedEquipment={mockProps.equipment[0]} />)
    const normalButtons = screen.getAllByText('Normal')
    const urgentButtons = screen.getAllByText('Urgent')
    const emergencyButtons = screen.getAllByText('EMERGENCY', { selector: 'button' })
    
    expect(normalButtons.length).toBe(2) // 2 pending requests for MRI
    expect(urgentButtons.length).toBe(2)
    expect(emergencyButtons.length).toBe(2)
  })

  test('clicking Normal approve button calls handleApprove', () => {
    render(<AdminView {...mockProps} selectedEquipment={mockProps.equipment[0]} />)
    const normalButton = screen.getAllByText('Normal')[0]
    fireEvent.click(normalButton)
    expect(mockProps.handleApprove).toHaveBeenCalledWith(1, 'NORMAL')
  })

  test('clicking Urgent approve button calls handleApprove', () => {
    render(<AdminView {...mockProps} selectedEquipment={mockProps.equipment[0]} />)
    const urgentButton = screen.getAllByText('Urgent')[0]
    fireEvent.click(urgentButton)
    expect(mockProps.handleApprove).toHaveBeenCalledWith(1, 'URGENT')
  })

  test('clicking Emergency approve button calls handleApprove', () => {
    render(<AdminView {...mockProps} selectedEquipment={mockProps.equipment[0]} />)
    const emergencyButton = screen.getAllByText('EMERGENCY')[0]
    fireEvent.click(emergencyButton)
    expect(mockProps.handleApprove).toHaveBeenCalledWith(1, 'EMERGENCY')
  })

  test('displays live queue for selected equipment', () => {
    render(<AdminView {...mockProps} selectedEquipment={mockProps.equipment[0]} />)
    expect(screen.getByText('Call Next Patient')).toBeInTheDocument()
    expect(screen.getByText('Alice Brown')).toBeInTheDocument()
    expect(screen.getByText('Charlie Davis')).toBeInTheDocument()
  })

  test('displays queue positions', () => {
    render(<AdminView {...mockProps} selectedEquipment={mockProps.equipment[0]} />)
    expect(screen.getByText('#1')).toBeInTheDocument()
    expect(screen.getByText('#2')).toBeInTheDocument()
  })

  test('displays priority tags', () => {
    render(<AdminView {...mockProps} selectedEquipment={mockProps.equipment[0]} />)
    // Priority tags in queue list (not buttons)
    const priorityTags = screen.getAllByText(/EMERGENCY|NORMAL/).filter(el => el.className && el.className.includes('prio-tag'))
    expect(priorityTags.length).toBeGreaterThanOrEqual(2)
  })

  test('clicking Call Next Patient button calls handleCallNext', () => {
    render(<AdminView {...mockProps} selectedEquipment={mockProps.equipment[0]} />)
    const callNextButton = screen.getByText('Call Next Patient')
    fireEvent.click(callNextButton)
    expect(mockProps.handleCallNext).toHaveBeenCalled()
  })

  test('shows empty queue message when no patients', () => {
    render(<AdminView {...mockProps} selectedEquipment={mockProps.equipment[0]} queue={[]} />)
    expect(screen.getByText('Operational queue empty.')).toBeInTheDocument()
  })

  test('displays formatted slot time', () => {
    render(<AdminView {...mockProps} selectedEquipment={mockProps.equipment[0]} />)
    expect(screen.getByText(/2026-02-15 09:00/)).toBeInTheDocument()
    expect(screen.getByText(/2026-02-15 10:30/)).toBeInTheDocument()
  })

  test('selected equipment has selected class', () => {
    render(<AdminView {...mockProps} selectedEquipment={mockProps.equipment[0]} />)
    const mriCard = screen.getByText('MRI-1').closest('.card')
    expect(mriCard).toHaveClass('selected')
  })
})
