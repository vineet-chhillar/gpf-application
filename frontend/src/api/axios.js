import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8081/api", // change port if needed
  headers: {
    "Content-Type": "application/json"
  }
});

export default api;
