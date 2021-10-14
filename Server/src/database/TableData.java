package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import database.TableSchema.Column;

// modella ll�insieme di tuple collezionate in una
// tabella. La singola tupla � modellata dalla classe Tuple_Data inner class di Table_Data.
public class TableData {

	// ATTRIBUTI

	private Connection connection;

	// COSTRUTTORE

	public TableData(Connection connection) {
		this.connection = connection;
	}

	// INNER CLASS

	public class TupleData {
		public List<Object> tuple = new ArrayList<Object>();

		public String toString() {
			String value = "";
			Iterator<Object> it = tuple.iterator();
			while (it.hasNext())
				value += (it.next().toString() + " ");

			return value;
		}
	}

	// METODI

	/**
	 * Ricava lo schema della tabella con nome table. Esegue una interrogazione per
	 * estrarre le tuple da tale tabella. Per ogni tupla del resultset, si crea un
	 * oggetto, istanza della classe Tupla, il cui riferimento va incluso nella
	 * lista da restituire. In particolare, per la tupla corrente nel resultset, si
	 * estraggono i valori dei singoli campi (usando getFloat() o getString()), e li
	 * si aggiungono all�oggetto istanza della classe Tupla che si sta costruendo.
	 */
	public List<TupleData> getTransazioni(String table) throws SQLException {
		LinkedList<TupleData> transSet = new LinkedList<TupleData>();
		Statement statement;
		TableSchema tSchema = new TableSchema(table, connection);

		String query = "select ";

		for (int i = 0; i < tSchema.getNumberOfAttributes(); i++) {
			Column c = tSchema.getColumn(i);
			if (i > 0)
				query += ",";
			query += c.getColumnName();
		}
		if (tSchema.getNumberOfAttributes() == 0)
			throw new SQLException();
		query += (" FROM " + table);

		statement = connection.createStatement();
		ResultSet rs = statement.executeQuery(query);
		while (rs.next()) {
			TupleData currentTuple = new TupleData();
			for (int i = 0; i < tSchema.getNumberOfAttributes(); i++)
				if (tSchema.getColumn(i).isNumber())
					currentTuple.tuple.add(rs.getFloat(i + 1));
				else
					currentTuple.tuple.add(rs.getString(i + 1));
			transSet.add(currentTuple);
		}
		rs.close();
		statement.close();

		return transSet;

	}

	/*
	 * Formula ed esegue una interrogazione SQL per estrarre i valori distinti
	 * ordinati di column e popolare una lista da restituire.
	 */
	public List<Object> getDistinctColumnValues(String table, Column column) throws SQLException {
		LinkedList<Object> valueSet = new LinkedList<Object>();
		Statement statement;
		TableSchema tSchema = new TableSchema(table, connection);

		String query = "select distinct ";

		query += column.getColumnName();

		query += (" FROM " + table);

		query += (" ORDER BY " + column.getColumnName());

		statement = connection.createStatement();
		ResultSet rs = statement.executeQuery(query);
		while (rs.next()) {
			if (column.isNumber())
				valueSet.add(rs.getFloat(1));
			else
				valueSet.add(rs.getString(1));

		}
		rs.close();
		statement.close();

		return valueSet;

	}

	/*
	 * Formula ed esegue una interrogazione SQL per estrarre il valore aggregato
	 * (valore minimo o valore massimo) cercato nella colonna di nome column della
	 * tabella di nome 3 table. Il metodo solleva e propaga una NoValueException se
	 * il resultset � vuoto o il valore calcolato � pari a null N.B. aggregate � di
	 * tipo QUERY_TYPE dove QUERY_TYPE � la classe enumerativa fornita dal docente
	 */
	public Object getAggregateColumnValue(String table, Column column, QUERY_TYPE aggregate)
			throws SQLException, NoValueException {
		Statement statement;
		TableSchema tSchema = new TableSchema(table, connection);
		Object value = null;
		String aggregateOp = "";

		String query = "select ";
		if (aggregate == QUERY_TYPE.MAX)
			aggregateOp += "max";
		else
			aggregateOp += "min";
		query += aggregateOp + "(" + column.getColumnName() + ") FROM " + table;

		statement = connection.createStatement();
		ResultSet rs = statement.executeQuery(query);
		if (rs.next()) {
			if (column.isNumber())
				value = rs.getFloat(1);
			else
				value = rs.getString(1);

		}
		rs.close();
		statement.close();
		if (value == null)
			throw new NoValueException("No " + aggregateOp + " on " + column.getColumnName());

		return value;

	}

}
