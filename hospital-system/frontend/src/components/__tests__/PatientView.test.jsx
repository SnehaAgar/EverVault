import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import PatientView from '../PatientView'

describe('PatientView Component', () => {
  const mockProps = {
    equipment: [
      { id: 1, name: 'MRI-1', status: 'AVAILABLE', nextAvailable: 'Now', queueLength: 0 },
      { id: 2, name: 'CT-Scanner', status: 'IN_USE', nextAvailable: '10:30', queueLength: 2 },
    ],
    selectedEquipment: null,
    setSelectedEquipment: jest.fn(),
    patientName: '',
    setPatientName: jest.fn(),
    slotTime: '',
    setSlotTime: jest.fn(),
    requestedPriority: 'NORMAL',
    setRequestedPriority: jest.fn(),
    handleRequestBooking: jest.fn((e) => e.preventDefault()),
  }

  beforeEach(() => {
    jest.clearAllMocks()
  })

  test('renders section header', () => {
    render(<PatientView {...mockProps} />)
    expect(screen.getByText('Patient Service Portal')).toBeInTheDocument()
    expect(screen.getByText(/Select a facility/)).toBeInTheDocument()
  })

  test('renders equipment list', () => {
    render(<PatientView {...mockProps} />)
    expect(screen.getByText('MRI-1')).toBeInTheDocument()
    expect(screen.getByText('CT-Scanner')).toBeInTheDocument()
  })

  test('shows loading message when equipment is empty', () => {
    render(<PatientView {...mockProps} equipment={[]} />)
    expect(screen.getByText(/Loading facilities/)).toBeInTheDocument()
  })

  test('displays equipment status correctly', () => {
    render(<PatientView {...mockProps} />)
    expect(screen.getByText('AVAILABLE')).toBeInTheDocument()
    expect(screen.getByText('IN USE')).toBeInTheDocument()
  })

  test('displays queue information', () => {
    render(<PatientView {...mockProps} />)
    // Check for equipment meta info
    expect(screen.getAllByText(/Next:/).length).toBeGreaterThanOrEqual(2)
    expect(screen.getByText('Now')).toBeInTheDocument()
    expect(screen.getByText('10:30')).toBeInTheDocument()
    expect(screen.getAllByText(/Wait:/).length).toBeGreaterThanOrEqual(2)
  })

  test('clicking equipment card selects it', () => {
    render(<PatientView {...mockProps} />)
    const mriCard = screen.getByText('MRI-1').closest('.eq-card')
    fireEvent.click(mriCard)
    expect(mockProps.setSelectedEquipment).toHaveBeenCalledWith(mockProps.equipment[0])
  })

  test('renders booking form', () => {
    render(<PatientView {...mockProps} />)
    expect(screen.getByText('Request a Booking')).toBeInTheDocument()
    expect(screen.getByPlaceholderText(/e.g. John Doe/)).toBeInTheDocument()
    expect(screen.getByText(/Preferred Date/)).toBeInTheDocument()
    expect(screen.getByText(/Service Urgency/)).toBeInTheDocument()
  })

  test('submit button is disabled when no equipment selected', () => {
    render(<PatientView {...mockProps} />)
    const submitButton = screen.getByText('Select a Facility')
    expect(submitButton).toBeDisabled()
  })

  test('submit button shows equipment name when selected', () => {
    render(<PatientView {...mockProps} selectedEquipment={mockProps.equipment[0]} />)
    expect(screen.getByText('Book MRI-1')).toBeInTheDocument()
    expect(screen.getByText('Book MRI-1')).not.toBeDisabled()
  })

  test('priority dropdown has correct options', () => {
    render(<PatientView {...mockProps} />)
    expect(screen.getByText('Standard Checkup')).toBeInTheDocument()
    expect(screen.getByText('Urgent Care Needed')).toBeInTheDocument()
    expect(screen.getByText(/Critical Emergency/)).toBeInTheDocument()
  })

  test('changing priority calls setRequestedPriority', () => {
    render(<PatientView {...mockProps} />)
    const select = screen.getByDisplayValue('Standard Checkup')
    fireEvent.change(select, { target: { value: 'EMERGENCY' } })
    expect(mockProps.setRequestedPriority).toHaveBeenCalledWith('EMERGENCY')
  })

  test('entering patient name calls setPatientName', () => {
    render(<PatientView {...mockProps} />)
    const input = screen.getByPlaceholderText(/e.g. John Doe/)
    fireEvent.change(input, { target: { value: 'Jane Smith' } })
    expect(mockProps.setPatientName).toHaveBeenCalledWith('Jane Smith')
  })

  test('submitting form calls handleRequestBooking', () => {
    render(<PatientView {...mockProps} selectedEquipment={mockProps.equipment[0]} />)
    const form = screen.getByText('Book MRI-1').closest('form')
    fireEvent.submit(form)
    expect(mockProps.handleRequestBooking).toHaveBeenCalled()
  })
})
