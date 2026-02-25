const BASE_URL = "http://10.1.60.34:8081/api/statusmaster";

async function handleResponse(res) {
  if (!res.ok) {
    const error = await res.json();
    throw error;
  }
  return res.json();
}


export async function fetchStatuses() {
  const res = await fetch(BASE_URL);
  return handleResponse(res);
}

export async function createStatus(payload) {
  const res = await fetch(BASE_URL, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });
 return handleResponse(res);
}

export async function updateStatus(id, payload) {
  const res = await fetch(`${BASE_URL}/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });
 return handleResponse(res);
}

export async function toggleStatusActive(id) {
  const res = await fetch(`${BASE_URL}/${id}/toggle-active`, {
    method: "PATCH"
  });
  return handleResponse(res);
}
