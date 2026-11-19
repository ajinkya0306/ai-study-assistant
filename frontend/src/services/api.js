const API = "http://localhost:8080";

export function getToken() {
    return localStorage.getItem("token");
}

export async function apiRequest(endpoint, options = {}) {
    const token = getToken();

    const headers = {
        ...(options.body instanceof FormData
            ? {}
            : { "Content-Type": "application/json" }),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers
    };

    const response = await fetch(`${API}${endpoint}`, {
        ...options,
        headers
    });

    if (response.status === 401 || response.status === 403) {
        throw new Error("Session expired. Please log in again.");
    }

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.error || data.message || "Request failed");
    }

    return data;
}