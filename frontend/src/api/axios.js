import axios from "axios";

const api = axios.create({
  baseURL: "http://10.1.60.34:8081/api", // change port if needed
  headers: {
    "Content-Type": "application/json"
  }
});

export default api;
