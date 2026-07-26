import { useState } from 'react'
import './App.css'
import { createDocument, readAuditEvent, readDocument } from './util/api'

const currentUser = { id: 1, username: 'tester1' } // TODO: replace with phase1 auth user.

const initialForm = {
  projectId: '1',
  dossierId: '1',
  title: '',
  documentNumber: '',
  type: 'DRAWING',
  originatingCompanyId: '',
  revisionCode: 'A',
  status: 'DRAFT',
  issuedAt: new Date().toISOString().slice(0, 16),
}

function App() {
  const [activeView, setActiveView] = useState('create')
  const [form, setForm] = useState(initialForm)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [result, setResult] = useState(null)
  const [documentId, setDocumentId] = useState('1')
  const [isReading, setIsReading] = useState(false)
  const [readResult, setReadResult] = useState(null)
  const [auditEventId, setAuditEventId] = useState('1')
  const [isReadingAuditEvent, setIsReadingAuditEvent] = useState(false)
  const [auditEventResult, setAuditEventResult] = useState(null)

  const updateField = (event) => {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
  }

  const submitForm = async (event) => {
    event.preventDefault()
    setIsSubmitting(true)
    setResult(null)

    const payload = {
      projectId: Number(form.projectId),
      dossierId: form.dossierId ? Number(form.dossierId) : null,
      title: form.title.trim(),
      documentNumber: form.documentNumber.trim(),
      type: form.type,
      originatingCompanyId: form.originatingCompanyId
        ? Number(form.originatingCompanyId)
        : null,
      revisionCode: form.revisionCode || null,
      status: form.status || null,
      issuedAt: form.issuedAt ? `${form.issuedAt}:00` : null,
    }

    try {
      const message = await createDocument(payload)
      setResult({ type: 'success', message })
    } catch (error) {
      setResult({ type: 'error', message: error.message })
    } finally {
      setIsSubmitting(false)
    }
  }

  const submitReadForm = async (event) => {
    event.preventDefault()
    setIsReading(true)
    setReadResult(null)

    try {
      const document = await readDocument(documentId)
      setReadResult({ type: 'success', document })
    } catch (error) {
      setReadResult({ type: 'error', message: error.message })
    } finally {
      setIsReading(false)
    }
  }

  const submitReadAuditEventForm = async (event) => {
    event.preventDefault()
    setIsReadingAuditEvent(true)
    setAuditEventResult(null)

    try {
      const auditEvent = await readAuditEvent(auditEventId)
      setAuditEventResult({ type: 'success', message: auditEvent })
    } catch (error) {
      setAuditEventResult({ type: 'error', message: error.message })
    } finally {
      setIsReadingAuditEvent(false)
    }
  }

  return (
    <main className="app-shell">
      <aside className="sidebar" aria-label="Document actions">
        <p>Signed in as {currentUser.username}</p>
        <button
          className={activeView === 'create' ? 'nav-button active' : 'nav-button'}
          type="button"
          onClick={() => setActiveView('create')}
        >
          Create Form
        </button>
        <button
          className={activeView === 'read' ? 'nav-button active' : 'nav-button'}
          type="button"
          onClick={() => setActiveView('read')}
        >
          Read Form
        </button>
        <button
          className={activeView === 'readAuditEvent' ? 'nav-button active' : 'nav-button'}
          type="button"
          onClick={() => setActiveView('readAuditEvent')}
        >
          Read Audit Event
        </button>
      </aside>

      <section className="form-panel">
        {activeView === 'create' && (
          <>
            <div className="heading">
              <h1>Create Document</h1>
            </div>

            <form onSubmit={submitForm}>
              <div className="field-row">
                <label>
                  Project ID
                  <input
                    name="projectId"
                    type="number"
                    value={form.projectId}
                    onChange={updateField}
                  />
                </label>

                <label>
                  Dossier ID
                  <input
                    name="dossierId"
                    type="number"
                    value={form.dossierId}
                    onChange={updateField}
                  />
                </label>
              </div>

              <label>
                Title
                <input name="title" value={form.title} onChange={updateField} />
              </label>

              <label>
                Document Number
                <input
                  name="documentNumber"
                  value={form.documentNumber}
                  onChange={updateField}
                />
              </label>

              <div className="field-row">
                <label>
                  Type
                  <select name="type" value={form.type} onChange={updateField}>
                    <option>DRAWING</option>
                    <option>SPEC</option>
                    <option>DATASHEET</option>
                    <option>SUBMITTAL</option>
                    <option>QUERY</option>
                    <option>RCO</option>
                    <option>CO</option>
                    <option>REPORT</option>
                  </select>
                </label>

                <label>
                  Status
                  <select name="status" value={form.status} onChange={updateField}>
                    <option>DRAFT</option>
                    <option>SUBMITTED</option>
                    <option>APPROVED_WITH_COMMENTS</option>
                    <option>IFC</option>
                    <option>REJECTED</option>
                    <option>CANCELLED</option>
                    <option>SUPERSEDED</option>
                  </select>
                </label>
              </div>

              <div className="field-row">
                <label>
                  Originating Company ID
                  <input
                    name="originatingCompanyId"
                    type="number"
                    value={form.originatingCompanyId}
                    onChange={updateField}
                  />
                </label>

                <label>
                  Revision Code
                  <input
                    name="revisionCode"
                    value={form.revisionCode}
                    onChange={updateField}
                  />
                </label>
              </div>

              <label>
                Issued At
                <input
                  name="issuedAt"
                  type="datetime-local"
                  value={form.issuedAt}
                  onChange={updateField}
                />
              </label>

              <button type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Creating...' : 'Create Document'}
              </button>
            </form>

            {result && (
              <p className={`result ${result.type}`} role="status">
                {result.message}
              </p>
            )}
          </>
        )}

        {activeView === 'read' && (
          <>
            <div className="heading">
              <h1>Read Document</h1>
            </div>

            <form onSubmit={submitReadForm}>
              <label>
                Document ID
                <input
                  min="1"
                  type="number"
                  value={documentId}
                  onChange={(event) => setDocumentId(event.target.value)}
                />
              </label>

              <button type="submit" disabled={isReading}>
                {isReading ? 'Reading...' : 'Read Document'}
              </button>
            </form>

            {readResult?.type === 'success' && (
              <dl className="document-result" role="status">
                {Object.entries(readResult.document).map(([key, value]) => (
                  <div key={key}>
                    <dt>{key}</dt>
                    <dd>{value === null ? 'null' : String(value)}</dd>
                  </div>
                ))}
              </dl>
            )}

            {readResult?.type === 'error' && (
              <p className="result error" role="status">
                {readResult.message}
              </p>
            )}
          </>
        )}

        {activeView === 'readAuditEvent' && (
          <>
            <div className="heading">
              <h1>Read Audit Event</h1>
            </div>

            <form onSubmit={submitReadAuditEventForm}>
              <label>
                Audit Event ID
                <input
                  min="1"
                  type="number"
                  value={auditEventId}
                  onChange={(event) => setAuditEventId(event.target.value)}
                />
              </label>

              <button type="submit" disabled={isReadingAuditEvent}>
                {isReadingAuditEvent ? 'Reading...' : 'Read Audit Event'}
              </button>
            </form>

            {auditEventResult?.type === 'success' && (
              <pre className="text-result" role="status">
                {auditEventResult.message}
              </pre>
            )}

            {auditEventResult?.type === 'error' && (
              <p className="result error" role="status">
                {auditEventResult.message}
              </p>
            )}
          </>
        )}
      </section>
    </main>
  )
}

export default App
