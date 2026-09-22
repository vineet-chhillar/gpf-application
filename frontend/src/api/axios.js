import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:9090/GPF/api", // change port if needed
  headers: {
    "Content-Type": "application/json"
  }
});

export default api;
