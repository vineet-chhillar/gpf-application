import "./StatusTable.css";


const StatusTable = ({ data, onEdit, onToggleActive }) => {
    console.log("Table data:", data);
  return (
    <div className="status-table-wrapper">
      <table className="status-table">
        <thead>
          <tr>
            <th>Status Code</th>
            <th>Final</th>
            <th>Active</th>
            <th>Action</th>
          </tr>
        </thead>

        <tbody>
          {data.map((row) => (
            <tr key={row.id}>
              <td className="status-code">{row.statusCode}</td>

              <td>
  <span
    className={`badge ${
      row.isFinal ? "badge-final" : "badge-nonfinal"
    }`}
  >
    {row.final ? "Final" : "In Flow"}
  </span>
</td>


              <td>
  <label className="switch">
    <input
      type="checkbox"
      checked={row.active}
      onChange={() => onToggleActive(row)}
    />
    <span className="slider"></span>
  </label>
</td>


              <td>
                <button
                  className="btn btn-secondary"
                  onClick={() => onEdit(row)}
                >
                  Edit
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default StatusTable;
