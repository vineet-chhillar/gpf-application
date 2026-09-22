import { BrowserRouter, Routes, Route } from "react-router-dom";
import MainLayout from "./Layout/MainLayout";

function App() {
  return (
    <BrowserRouter basename="/GPF">
      <Routes>
        <Route path="/*" element={<MainLayout />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;