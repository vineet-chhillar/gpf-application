import { BrowserRouter, Routes, Route } from "react-router-dom";
import MainLayout from "./Layout/MainLayout";
import { AuthProvider } from "./context/AuthContext";
function App() {
   console.log("App component rendered");
  return (
    <BrowserRouter basename="/GPF">
      <AuthProvider>
        <Routes>
          <Route path="/*" element={<MainLayout />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;