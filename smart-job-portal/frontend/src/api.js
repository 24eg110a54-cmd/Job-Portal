import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// An expired or invalid token sends the user back to the login page.
// We dispatch a custom event so AuthContext can clear its in-memory state (H-3).
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const onAuthPage = ["/login", "/register"].includes(window.location.pathname);
    if (err.response?.status === 401 && !onAuthPage) {
      window.dispatchEvent(new Event("auth:logout"));
    }
    return Promise.reject(err);
  }
);

export const errorMessage = (err, fallback = "Something went wrong. Try again.") =>
  err.response?.data?.error || fallback;

export default api;
