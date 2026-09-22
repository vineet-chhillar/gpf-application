import React from "react";

const ErrorModal = ({ show, message, onClose }) => {
  if (!show) return null;

  return (
    <div style={overlayStyle}>
      <div style={modalStyle}>
        <h3 style={{ color: "#e53935" }}>Error</h3>
        <p>{message}</p>
        <button onClick={onClose} style={btnStyle}>OK</button>
      </div>
    </div>
  );
};

const overlayStyle = {
  position: "fixed",
  top: 0,
  left: 0,
  width: "100%",
  height: "100%",
  backgroundColor: "rgba(0,0,0,0.5)",
  display: "flex",
  justifyContent: "center",
  alignItems: "center",
  zIndex: 9999,
};

const modalStyle = {
  background: "#fff",
  padding: "20px",
  borderRadius: "8px",
  minWidth: "300px",
  textAlign: "center",
};

const btnStyle = {
  marginTop: "15px",
  padding: "8px 16px",
  backgroundColor: "#4e1d7c",
  color: "#fff",
  border: "none",
  borderRadius: "4px",
  cursor: "pointer",
};

export default ErrorModal;