import axios from "axios";

const leaveApi = axios.create({
  baseURL: "http://localhost:8081/api",
});

export default leaveApi;