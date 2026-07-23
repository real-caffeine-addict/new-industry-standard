import { useState } from 'react'
import './App.css'

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
  const [form, setForm] = useState(initialForm)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [result, setResult] = useState(null)

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
      const response = await fetch('http://localhost:8083/create-document', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      })

      const message = await response.text()

      if (!response.ok) {
        throw new Error(message || 'Failed to create document')
      }

      setResult({ type: 'success', message })
    } catch (error) {
      setResult({ type: 'error', message: error.message })
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="app-shell">
      <section className="form-panel">
        <div className="heading">
          <p>Signed in as {currentUser.username}</p>
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
      </section>
    </main>
  )
}

export default App
