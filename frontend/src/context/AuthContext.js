import {
  createContext,
  useContext,
  useEffect,
  useState
} from "react";

import api from "../api/axios";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [employee, setEmployee] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function loadEmployee() {
      try {
        const response = await api.get("/auth/me", {
          withCredentials: true,
          headers: {
            Accept: "application/json"
          }
        });

        setEmployee(response.data);
        setError(null);
      } catch (err) {
        console.error("Failed to load employee details:", err);
        setEmployee(null);
        setError(
          err.response?.status === 401
            ? "Authentication required"
            : err.response?.data?.message ||
              err.message ||
              "Unable to load employee details"
        );
      } finally {
        setLoading(false);
      }
    }

    loadEmployee();
  }, []);
useEffect(() => {
  const handlePageHide = () => {
    setEmployee(null);
    setError(null);
  };

  window.addEventListener("pagehide", handlePageHide);

  return () => {
    window.removeEventListener("pagehide", handlePageHide);
  };
}, []);
 const logout = async () => {
  try {
    // Call the PHP/backend endpoint that destroys the session
    await api.post(
      "/auth/logout",
      {},
      {
        withCredentials: true,
        headers: {
          Accept: "application/json"
        }
      }
    );
  } catch (err) {
    console.error("Logout API failed:", err);
  } finally {
    // Clear React authentication state
    setEmployee(null);
    setError(null);
    setLoading(false);

    // Redirect to the parent application's index page
    window.location.replace("/GPF/index.php");
  }
};

  return (
    <AuthContext.Provider
      value={{ employee, loading, error, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}