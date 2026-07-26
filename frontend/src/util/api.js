const DOCUMENTS_BASE_URL = 'http://localhost:8083/documents'
const AUDIT_EVENTS_BASE_URL = 'http://localhost:8084/audit-events'

export async function createDocument(payload) {
  const response = await fetch(`${DOCUMENTS_BASE_URL}`, {
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

  return message
}

export async function readDocument(id) {
  const response = await fetch(`${DOCUMENTS_BASE_URL}/${id}`)
  const contentType = response.headers.get('content-type') || ''
  const body = contentType.includes('application/json')
    ? await response.json()
    : await response.text()

  if (!response.ok) {
    throw new Error(
      typeof body === 'string' ? body || 'Failed to read document' : 'Failed to read document',
    )
  }

  return body
}

export async function readAuditEvent(id) {
  const response = await fetch(`${AUDIT_EVENTS_BASE_URL}/${id}`)
  const message = await response.text()

  if (!response.ok) {
    throw new Error(message || 'Failed to read audit event')
  }

  return message
}
